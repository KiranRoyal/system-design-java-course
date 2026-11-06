package course;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.LongSupplier;

/** Bounded, process-local LRU cache. TTL uses a monotonic clock; reads do not renew it. */
public final class TtlCache<K,V> {
    private record Entry<V>(V value, long writtenAt) {}
    private final LinkedHashMap<K,Entry<V>> entries = new LinkedHashMap<>(16, .75f, true);
    private final int capacity;
    private final long ttlNanos;
    private final LongSupplier clock;
    private long hits, misses;
    public TtlCache(int capacity, Duration ttl, LongSupplier clock) {
        if (capacity < 1 || ttl.isNegative() || ttl.isZero()) throw new IllegalArgumentException();
        this.capacity = capacity; this.ttlNanos = ttl.toNanos(); this.clock = clock;
    }
    public synchronized V get(K key) {
        var e = entries.get(key);
        if (e == null || clock.getAsLong() - e.writtenAt >= ttlNanos) {
            entries.remove(key); misses++; return null;
        }
        hits++; return e.value;
    }
    public synchronized void put(K key, V value) {
        entries.put(Objects.requireNonNull(key), new Entry<>(Objects.requireNonNull(value), clock.getAsLong()));
        while (entries.size() > capacity) entries.remove(entries.keySet().iterator().next());
    }
    public synchronized void invalidate(K key) { entries.remove(key); }
    public synchronized long hits() { return hits; }
    public synchronized long misses() { return misses; }
}
