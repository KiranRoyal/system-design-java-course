package course;
import java.util.*;

/** Atomicity inside ONE JVM via a monitor. Not durable, not a database transaction. */
public final class OrderStore {
    public record Order(String id, long amountCents, String status) {}
    public record Result(Order order, boolean replayed) {}
    public record Event(String id, String orderId, String type) {}
    public static final class Conflict extends RuntimeException {}
    public static final class Full extends RuntimeException {}
    private final Map<String,Order> byKey = new HashMap<>(), byId = new HashMap<>();
    private final LinkedHashMap<String,Event> outbox = new LinkedHashMap<>();
    private final int capacity;
    public OrderStore(int capacity) { if(capacity<1) throw new IllegalArgumentException(); this.capacity=capacity; }
    public synchronized Result create(String key, long amount) {
        if (key==null || !key.matches("[A-Za-z0-9_-]{1,100}") || amount<1 || amount>100_000_000)
            throw new IllegalArgumentException("Invalid idempotency key or amount");
        var previous=byKey.get(key);
        if(previous!=null) {
            if(previous.amountCents != amount) throw new Conflict();
            return new Result(previous,true);
        }
        if(byId.size()>=capacity) throw new Full();
        var order=new Order(UUID.randomUUID().toString(),amount,"ACCEPTED");
        var event=new Event(UUID.randomUUID().toString(),order.id,"ORDER_ACCEPTED");
        // All three writes share a critical section. A process crash still loses everything.
        byKey.put(key,order); byId.put(order.id,order); outbox.put(event.id,event);
        return new Result(order,false);
    }
    public synchronized Order get(String id) { return byId.get(id); }
    public synchronized List<Event> pending() { return List.copyOf(outbox.values()); }
    public synchronized void acknowledge(String eventId) { outbox.remove(eventId); }
    public synchronized int count() { return byId.size(); }
    public synchronized int pendingCount() { return outbox.size(); }
    public static final class Consumer {
        private final Set<String> seen=new HashSet<>();
        private int effects;
        public synchronized boolean accept(Event e) {
            if(!seen.add(e.id)) return false;
            effects++; return true;
        }
        public synchronized int effects() { return effects; }
    }
}
