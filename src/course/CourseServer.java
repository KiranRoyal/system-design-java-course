package course;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/** Local teaching server: no persistent storage, TLS or authentication. */
public final class CourseServer implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService executor=Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore admission=new Semaphore(64);
    private final Map<String,String> links=new HashMap<>();
    private final TtlCache<String,String> cache=new TtlCache<>(100,Duration.ofSeconds(30),System::nanoTime);
    private final TokenBucket limiter=new TokenBucket(5,1,System::nanoTime);
    private final OrderStore orders=new OrderStore(1000);
    private final OrderStore.Consumer consumer=new OrderStore.Consumer();
    private final AtomicLong requests=new AtomicLong();
    private CourseServer(int port) throws IOException {
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",port),64);
        server.setExecutor(executor);
        server.createContext("/",this::handle); server.start();
    }
    public static CourseServer start(int port) throws IOException { return new CourseServer(port); }
    public int port() { return server.getAddress().getPort(); }
    @Override public void close() { server.stop(0); executor.shutdownNow(); }
    private static String quote(String value) {
        var b=new StringBuilder("\"");
        for(char c:value.toCharArray()) {
            switch(c) {
                case '"' -> b.append("\\\""); case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n"); case '\r' -> b.append("\\r"); case '\t' -> b.append("\\t");
                default -> { if(c<32) b.append(String.format("\\u%04x",(int)c)); else b.append(c); }
            }
        }
        return b.append('"').toString();
    }
    private static void send(HttpExchange x,int code,String body) throws IOException {
        x.getResponseHeaders().set("Cache-Control","no-store");
        if(code==204 || code==302) { x.sendResponseHeaders(code,-1); return; }
        byte[] bytes=body.getBytes(StandardCharsets.UTF_8);
        x.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
        x.sendResponseHeaders(code,bytes.length); x.getResponseBody().write(bytes);
    }
    private static void error(HttpExchange x,int code,String message) throws IOException {
        send(x,code,"{\"error\":"+quote(message)+"}");
    }
    private static boolean method(HttpExchange x,String expected) throws IOException {
        if(expected.equals(x.getRequestMethod())) return true;
        x.getResponseHeaders().set("Allow",expected); error(x,405,"Method not allowed"); return false;
    }
    private static String body(HttpExchange x) throws IOException {
        String contentType=x.getRequestHeaders().getFirst("Content-Type");
        if(contentType==null || !contentType.split(";",2)[0].trim().equalsIgnoreCase("text/plain"))
            throw new BadRequest(415,"Use Content-Type text/plain");
        byte[] bytes=x.getRequestBody().readNBytes(4097);
        if(bytes.length>4096) throw new BadRequest(413,"Body too large");
        return new String(bytes,StandardCharsets.UTF_8).trim();
    }
    private static class BadRequest extends RuntimeException {
        final int status;
        BadRequest(int status,String message) { super(message); this.status=status; }
    }
    private void handle(HttpExchange x) throws IOException {
        requests.incrementAndGet();
        boolean admitted=admission.tryAcquire();
        try {
            if(!admitted) { x.getResponseHeaders().set("Retry-After","1"); error(x,503,"Busy"); return; }
            route(x);
        } catch(BadRequest e) { error(x,e.status,e.getMessage()); }
        catch(OrderStore.Conflict e) { error(x,409,"Key reused with different amount"); }
        catch(OrderStore.Full e) { error(x,503,"Teaching store full; restart to reset"); }
        catch(IllegalArgumentException e) { error(x,400,"Invalid request"); }
        catch(Exception e) { error(x,500,"Unexpected failure"); }
        finally { if(admitted) admission.release(); x.close(); }
    }
    private void route(HttpExchange x) throws IOException {
        String path=x.getRequestURI().getPath();
        if(path.equals("/health")) { if(method(x,"GET")) send(x,200,"{\"status\":\"UP\"}"); }
        else if(path.equals("/links")) {
            if(!method(x,"POST")) return;
            String url=body(x);
            URI uri=URI.create(url);
            if(!Set.of("http","https").contains(Objects.toString(uri.getScheme(),"").toLowerCase(Locale.ROOT))
                || uri.getHost()==null || uri.getUserInfo()!=null || url.chars().anyMatch(c->c<=32 || c>=127))
                throw new IllegalArgumentException("Invalid URL");
            String id=UUID.randomUUID().toString().replace("-","").substring(0,12);
            synchronized(links) {
                if(links.size()>=1000) throw new OrderStore.Full();
                while(links.containsKey(id)) id=UUID.randomUUID().toString().replace("-","").substring(0,12);
                links.put(id,url);
            }
            x.getResponseHeaders().set("Location","/r/"+id);
            send(x,201,"{\"id\":"+quote(id)+",\"url\":"+quote(url)+"}");
        } else if(path.startsWith("/r/")) {
            if(!method(x,"GET")) return;
            String id=path.substring(3), url;
            // Serialize store+cache access with deletes to prevent stale fill after invalidation.
            synchronized(links) {
                url=cache.get(id);
                if(url==null) { url=links.get(id); if(url!=null) cache.put(id,url); }
            }
            if(url==null) error(x,404,"Unknown link");
            else { x.getResponseHeaders().set("Location",url); send(x,302,""); }
        } else if(path.startsWith("/links/")) {
            if(!method(x,"DELETE")) return;
            String id=path.substring(7);
            synchronized(links) { links.remove(id); cache.invalidate(id); }
            send(x,204,"");
        } else if(path.equals("/orders")) {
            if(!method(x,"POST")) return;
            long amount=Long.parseLong(body(x));
            var result=orders.create(x.getRequestHeaders().getFirst("Idempotency-Key"),amount);
            send(x,result.replayed()?200:201,orderJson(result.order(),result.replayed()));
        } else if(path.startsWith("/orders/")) {
            if(!method(x,"GET")) return;
            var order=orders.get(path.substring(8));
            if(order==null) error(x,404,"Unknown order"); else send(x,200,orderJson(order,false));
        } else if(path.equals("/admin/dispatch")) {
            if(!method(x,"POST")) return;
            int delivered=0;
            for(var event:orders.pending()) {
                if(consumer.accept(event)) delivered++;
                orders.acknowledge(event.id());
            }
            send(x,200,"{\"delivered\":"+delivered+"}");
        } else if(path.equals("/limited")) {
            if(!method(x,"GET")) return;
            if(limiter.allow()) send(x,200,"{\"accepted\":true}");
            else { x.getResponseHeaders().set("Retry-After","1"); error(x,429,"Quota exhausted"); }
        } else if(path.equals("/metrics")) {
            if(!method(x,"GET")) return;
            send(x,200,"{\"requests\":"+requests.get()+",\"cacheHits\":"+cache.hits()
                +",\"cacheMisses\":"+cache.misses()+",\"orders\":"+orders.count()
                +",\"pendingEvents\":"+orders.pendingCount()+",\"consumedEvents\":"+consumer.effects()+"}");
        } else error(x,404,"Unknown endpoint");
    }
    private static String orderJson(OrderStore.Order o,boolean replayed) {
        return "{\"id\":"+quote(o.id())+",\"amountCents\":"+o.amountCents()
            +",\"status\":"+quote(o.status())+",\"replayed\":"+replayed+"}";
    }
    public static void main(String[] args) throws Exception {
        var app=start(args.length==0?18080:Integer.parseInt(args[0]));
        Runtime.getRuntime().addShutdownHook(new Thread(app::close));
        System.out.println("Teaching API http://127.0.0.1:"+app.port()+" (Ctrl+C to stop)");
        new CountDownLatch(1).await();
    }
}
