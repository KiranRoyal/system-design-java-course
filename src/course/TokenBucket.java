package course;
import java.util.function.LongSupplier;

/** A single local quota, not a distributed or per-user limiter. */
public final class TokenBucket {
    private final double capacity, perSecond;
    private final LongSupplier clock;
    private double tokens;
    private long last;
    public TokenBucket(int capacity, double perSecond, LongSupplier clock) {
        if (capacity <= 0 || !Double.isFinite(perSecond) || perSecond <= 0) throw new IllegalArgumentException();
        this.capacity = capacity; this.perSecond = perSecond; this.tokens = capacity;
        this.clock = clock; this.last = clock.getAsLong();
    }
    public synchronized boolean allow() {
        long now = clock.getAsLong();
        tokens = Math.min(capacity, tokens + Math.max(0, now - last) / 1_000_000_000.0 * perSecond);
        last = now;
        if (tokens < 1) return false;
        tokens -= 1; return true;
    }
}
