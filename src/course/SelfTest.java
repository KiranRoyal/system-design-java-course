package course;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.net.URI;
import java.net.http.*;
import java.io.IOException;

/** No test framework required. Fails with nonzero exit on violated behavior. */
public final class SelfTest {
    private static int checks;
    static void check(boolean condition,String description) {
        checks++; if(!condition)throw new AssertionError(description);
    }
    static void expect(Class<? extends Throwable> type,Callable<?> action,String why) throws Exception {
        try { action.call(); throw new AssertionError("Expected "+type+": "+why); }
        catch(Throwable e) { if(!type.isInstance(e))throw new AssertionError(why,e); checks++; }
    }
    static HttpResponse<String> request(HttpClient c,int port,String method,String path,String body,String key) throws Exception {
        var b=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).timeout(Duration.ofSeconds(5));
        if(key!=null)b.header("Idempotency-Key",key);
        if(body!=null)b.header("Content-Type","text/plain");
        return c.send(b.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    static String id(String json) { return json.split("\"id\":\"")[1].split("\"")[0]; }
    public static void main(String[] args) throws Exception {
        var clock=new AtomicLong();
        var cache=new TtlCache<String,String>(2,Duration.ofSeconds(1),clock::get);
        cache.put("a","A"); cache.put("b","B"); check(cache.get("a").equals("A"),"cache hit");
        cache.put("c","C");check(cache.get("b")==null,"least recently used eviction");
        clock.set(999_999_999);check(cache.get("a")!=null,"before expiry");
        clock.incrementAndGet();check(cache.get("a")==null,"exact expiry");
        cache.put("d","D");cache.invalidate("d");check(cache.get("d")==null,"invalidation");
        var bucket=new TokenBucket(2,2,clock::get);
        check(bucket.allow()&&bucket.allow()&&!bucket.allow(),"burst cap");
        clock.addAndGet(499_999_999);check(!bucket.allow(),"fractional refill insufficient");
        clock.incrementAndGet();check(bucket.allow()&&!bucket.allow(),"exact refill boundary");
        var raceBucket=new TokenBucket(10,1,clock::get);var accepted=new AtomicInteger();
        try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            var futures=new ArrayList<Future<?>>();
            for(int i=0;i<100;i++)futures.add(pool.submit(()->{if(raceBucket.allow())accepted.incrementAndGet();}));
            for(var f:futures)f.get();
        }
        check(accepted.get()==10,"concurrent token budget is atomic");
        var ring=new HashRing(64);ring.add("A");ring.add("B");
        var placement=new HashMap<String,String>();for(int i=0;i<1000;i++)placement.put("k"+i,ring.owner("k"+i));
        ring.add("C");
        check(placement.entrySet().stream().allMatch(e->ring.owner(e.getKey()).equals(e.getValue())||ring.owner(e.getKey()).equals("C")),"adding node moves keys only to new node");
        ring.remove("C");check(placement.entrySet().stream().allMatch(e->ring.owner(e.getKey()).equals(e.getValue())),"removal restores old ring");
        var cb=new CircuitBreaker(2,Duration.ofSeconds(1),clock::get);
        expect(IOException.class,()->cb.call(()->{throw new IOException();}),"first failure");
        expect(IOException.class,()->cb.call(()->{throw new IOException();}),"second failure");
        expect(CircuitBreaker.Rejected.class,()->cb.call(()->"bad"),"open rejects");
        clock.addAndGet(1_000_000_000);
        check(cb.call(()->"OK").equals("OK")&&cb.state()==CircuitBreaker.State.CLOSED,"probe closes breaker");
        var slow=new CircuitBreaker(1,Duration.ofSeconds(1),clock::get);
        var started=new CountDownLatch(1);var release=new CountDownLatch(1);
        try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            var old=pool.submit(()->slow.call(()->{started.countDown();release.await();return "late";}));
            check(started.await(5,TimeUnit.SECONDS),"inflight call entered");
            expect(IOException.class,()->slow.call(()->{throw new IOException();}),"concurrent failure opens");
            release.countDown();old.get(5,TimeUnit.SECONDS);
            check(slow.state()==CircuitBreaker.State.OPEN,"stale success cannot close breaker");
            clock.addAndGet(1_000_000_000);
            var probeEntered=new CountDownLatch(1);var probeDone=new CountDownLatch(1);
            var probe=pool.submit(()->slow.call(()->{probeEntered.countDown();probeDone.await();return "OK";}));
            check(probeEntered.await(5,TimeUnit.SECONDS),"probe entered");
            clock.addAndGet(10_000_000_000L);
            expect(CircuitBreaker.Rejected.class,()->slow.call(()->"bad"),"only one half-open probe even after cooldown");
            probeDone.countDown();probe.get(5,TimeUnit.SECONDS);
        }
        var store=new OrderStore(3);var ids=ConcurrentHashMap.<String>newKeySet();
        try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            var futures=new ArrayList<Future<?>>();
            for(int i=0;i<100;i++)futures.add(pool.submit(()->ids.add(store.create("same-key",500).order().id())));
            for(var f:futures)f.get();
        }
        check(ids.size()==1&&store.count()==1&&store.pendingCount()==1,"100 concurrent retries create one order and event");
        expect(OrderStore.Conflict.class,()->store.create("same-key",501),"different request conflicts");
        var consumer=new OrderStore.Consumer();var event=store.pending().getFirst();
        check(consumer.accept(event)&&!consumer.accept(event)&&consumer.effects()==1,"duplicate delivery one effect");
        store.acknowledge(event.id());check(store.pendingCount()==0,"outbox acknowledgement");
        store.create("2",1);store.create("3",1);
        expect(OrderStore.Full.class,()->store.create("4",1),"bounded store");
        check(store.create("same-key",500).replayed(),"replay works when full");
        var replicas=new ReplicationDemo();var v1=replicas.write("k","1");replicas.replicate("k",v1);
        var v2=replicas.write("k","2");check(replicas.readFollower("k").equals(v1),"stale follower shown");
        check(replicas.readYourWrites("k",v2.version()).equals(v2),"session read falls back");
        replicas.replicate("k",v2);replicas.replicate("k",v1);check(replicas.readFollower("k").equals(v2),"old version ignored");
        var bloom=new BloomFilter(10000,7);for(int i=0;i<1000;i++)bloom.add("k"+i);
        for(int i=0;i<1000;i++)if(!bloom.mightContain("k"+i))throw new AssertionError("False negative");
        check(true,"1000 inserted keys have no false negatives");
        if (Arrays.asList(args).contains("--unit")) {
            System.out.println("PASS: "+checks+" core behavior checks (HTTP integration not run in unit mode).");
            return;
        }
        try(var app=CourseServer.start(0);var client=HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()) {
            int p=app.port();
            check(request(client,p,"GET","/health",null,null).statusCode()==200,"health");
            check(request(client,p,"POST","/health",null,null).statusCode()==405,"method validation");
            check(request(client,p,"GET","/missing",null,null).statusCode()==404,"unknown endpoint");
            check(request(client,p,"POST","/links","javascript:alert(1)",null).statusCode()==400,"scheme validation");
            check(request(client,p,"POST","/links","x".repeat(4097),null).statusCode()==413,"body bound");
            check(request(client,p,"POST","/links",null,null).statusCode()==415,"content type");
            var link=request(client,p,"POST","/links","https://example.com/guide",null);
            check(link.statusCode()==201,"link created");String linkId=id(link.body());
            var redirect=request(client,p,"GET","/r/"+linkId,null,null);
            check(redirect.statusCode()==302 && redirect.headers().firstValue("Location").orElse("").equals("https://example.com/guide"),"redirect target");
            request(client,p,"GET","/r/"+linkId,null,null);
            check(request(client,p,"GET","/metrics",null,null).body().contains("\"cacheHits\":1"),"second read hits cache");
            check(request(client,p,"DELETE","/links/"+linkId,null,null).statusCode()==204,"delete");
            check(request(client,p,"GET","/r/"+linkId,null,null).statusCode()==404,"deletion invalidates cache");
            var order=request(client,p,"POST","/orders","12500","checkout-1");
            var retry=request(client,p,"POST","/orders","12500","checkout-1");
            check(order.statusCode()==201 && retry.statusCode()==200 && id(order.body()).equals(id(retry.body())),"HTTP idempotency");
            check(request(client,p,"POST","/orders","12600","checkout-1").statusCode()==409,"HTTP conflict");
            check(request(client,p,"POST","/orders","0","bad").statusCode()==400,"amount validation");
            check(request(client,p,"POST","/orders","100",null).statusCode()==400,"key required");
            check(request(client,p,"GET","/orders/"+id(order.body()),null,null).statusCode()==200,"order lookup");
            check(request(client,p,"POST","/admin/dispatch",null,null).body().contains("\"delivered\":1"),"event delivered");
            check(request(client,p,"POST","/admin/dispatch",null,null).body().contains("\"delivered\":0"),"dispatch idempotent");
            boolean limited=false;
            for(int i=0;i<15;i++)if(request(client,p,"GET","/limited",null,null).statusCode()==429)limited=true;
            check(limited,"HTTP quota enforced");
        }
        System.out.println("PASS: "+checks+" behavior checks, including concurrency and local HTTP integration.");
    }
}
