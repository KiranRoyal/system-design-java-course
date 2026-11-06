package course;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** Run one lab, for example: java -cp build/classes course.Labs cache */
public final class Labs {
    static void heading(String s) { System.out.println("\n--- "+s+" ---"); }
    public static void main(String[] args) throws Exception {
        String selected=args.length==0?"all":args[0];
        var names=List.of("capacity","cache","limiter","hashing","breaker","outbox","replication","bloom","queue","bulkhead");
        if(!selected.equals("all") && !names.contains(selected)) throw new IllegalArgumentException("Choose "+names);
        for(String name:names) {
            if(!selected.equals("all") && !selected.equals(name)) continue;
            heading(name);
            switch(name) {
                case "capacity" -> {
                    double rps=1_000_000.0*20/86400;
                    System.out.printf(Locale.ROOT,"Average %.2f req/s; 10x peak %.2f req/s%n",rps,rps*10);
                    System.out.printf(Locale.ROOT,"At mean 200 ms, peak in-flight %.0f requests%n",rps*10*.2);
                    System.out.println("10 million 500-byte rows/day = 5.0 GB/day raw, before replicas/indexes.");
                }
                case "cache" -> {
                    var clock=new AtomicLong(); var cache=new TtlCache<String,String>(2,Duration.ofSeconds(5),clock::get);
                    cache.put("product:1","price=100"); System.out.println("Hit: "+cache.get("product:1"));
                    clock.set(Duration.ofSeconds(5).toNanos());
                    System.out.println("At expiry: "+cache.get("product:1"));
                    System.out.println("hits="+cache.hits()+" misses="+cache.misses());
                }
                case "limiter" -> {
                    var clock=new AtomicLong(); var b=new TokenBucket(3,2,clock::get);
                    for(int i=0;i<4;i++) System.out.println("Request "+i+" allowed="+b.allow());
                    clock.addAndGet(500_000_000); System.out.println("After 0.5 s allowed="+b.allow());
                }
                case "hashing" -> {
                    var ring=new HashRing(128); for(String n:List.of("A","B","C"))ring.add(n);
                    var before=new HashMap<String,String>();
                    for(int i=0;i<10000;i++)before.put("key"+i,ring.owner("key"+i));
                    ring.add("D"); long moved=before.entrySet().stream().filter(e->!e.getValue().equals(ring.owner(e.getKey()))).count();
                    System.out.println("Keys moved after adding D: "+moved+" / 10000 (distribution is not exact)");
                }
                case "breaker" -> {
                    var clock=new AtomicLong(); var cb=new CircuitBreaker(2,Duration.ofSeconds(5),clock::get);
                    for(int i=0;i<3;i++)try { cb.call(()->{throw new java.io.IOException("dependency unavailable");}); }
                    catch(Exception e) { System.out.println(e.getClass().getSimpleName()+" state="+cb.state()); }
                    clock.addAndGet(5_000_000_000L);
                    System.out.println("Recovery probe: "+cb.call(()->"OK")+" state="+cb.state());
                }
                case "outbox" -> {
                    var store=new OrderStore(100); var c=new OrderStore.Consumer();
                    var first=store.create("checkout-1",12900); var replay=store.create("checkout-1",12900);
                    System.out.println("Same order on retry="+first.order().id().equals(replay.order().id()));
                    var e=store.pending().getFirst(); c.accept(e);
                    System.out.println("Simulated crash AFTER effect, BEFORE acknowledgement");
                    c.accept(e); store.acknowledge(e.id());
                    System.out.println("Effects after duplicate delivery="+c.effects());
                    System.out.println("This simulates relay interruption; consumer process/state did NOT restart.");
                }
                case "replication" -> {
                    var db=new ReplicationDemo(); var v1=db.write("cart","one item"); db.replicate("cart",v1);
                    var v2=db.write("cart","two items");
                    System.out.println("Stale follower="+db.readFollower("cart"));
                    System.out.println("Read-your-writes="+db.readYourWrites("cart",v2.version()));
                    db.replicate("cart",v2); db.replicate("cart",v1);
                    System.out.println("Old delivery cannot overwrite newer version="+db.readFollower("cart"));
                }
                case "bloom" -> {
                    var f=new BloomFilter(10000,7); for(int i=0;i<1000;i++)f.add("present"+i);
                    int fp=0; for(int i=0;i<10000;i++)if(f.mightContain("absent"+i))fp++;
                    System.out.println("Known key="+f.mightContain("present1")+"; false positives="+fp+"/10000");
                }
                case "queue" -> {
                    var queue=new ArrayBlockingQueue<Integer>(2);
                    System.out.println("Offer 1="+queue.offer(1)+" offer 2="+queue.offer(2)+" offer 3="+queue.offer(3));
                    System.out.println("Consume="+queue.take()+"; retry offer 3="+queue.offer(3));
                    System.out.println("Capacity bounds memory. A failed offer needs reject, delay, or durable retry policy.");
                }
                case "bulkhead" -> {
                    var slots=new Semaphore(3); var active=new AtomicInteger();var peak=new AtomicInteger();
                    try(var executor=Executors.newVirtualThreadPerTaskExecutor()) {
                        var work=new ArrayList<Future<?>>();
                        for(int i=0;i<20;i++) work.add(executor.submit(()->{
                            try { slots.acquire(); int n=active.incrementAndGet(); peak.accumulateAndGet(n,Math::max);
                                try { Thread.sleep(20); } finally { active.decrementAndGet();slots.release(); }
                            } catch(InterruptedException e) { Thread.currentThread().interrupt(); }
                        }));
                        for(var f:work) f.get();
                    }
                    System.out.println("Maximum simultaneous work="+peak.get()+" despite 20 virtual threads");
                    System.out.println("Production callers also need bounded waiting/deadlines; use tryAcquire at ingress.");
                }
            }
        }
    }
}
