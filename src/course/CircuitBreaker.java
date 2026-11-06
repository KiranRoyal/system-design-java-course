package course;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.LongSupplier;

/** Consecutive-failure breaker with one half-open probe. Does not impose a call timeout. */
public final class CircuitBreaker {
    public enum State { CLOSED, OPEN, HALF_OPEN }
    public static final class Rejected extends RuntimeException {}
    private record Permit(long generation, boolean probe) {}
    private final int threshold;
    private final long coolDown;
    private final LongSupplier clock;
    private int failures;
    private long openedAt, generation;
    private State state = State.CLOSED;
    public CircuitBreaker(int threshold, Duration coolDown, LongSupplier clock) {
        if (threshold < 1 || coolDown.isNegative() || coolDown.isZero()) throw new IllegalArgumentException();
        this.threshold=threshold; this.coolDown=coolDown.toNanos(); this.clock=clock;
    }
    private synchronized Permit acquire() {
        if (state == State.OPEN && clock.getAsLong()-openedAt >= coolDown) state=State.HALF_OPEN;
        if (state == State.OPEN) throw new Rejected();
        if (state == State.HALF_OPEN) {
            // The first acquire after cooldown owns the probe. Incrementing generation
            // also makes results from calls admitted before opening harmless.
            state = State.OPEN; openedAt = clock.getAsLong();
            // A separate probe flag prevents cooldown admitting another probe.
            probeActive=true;
            return new Permit(++generation,true);
        }
        return new Permit(generation,false);
    }
    private boolean probeActive;
    public <T> T call(Callable<T> action) throws Exception {
        Permit p;
        synchronized(this) { if (probeActive) throw new Rejected(); p=acquire(); }
        try { T result=action.call(); complete(p,true); return result; }
        catch (Exception e) { complete(p,false); throw e; }
        catch (Error e) { complete(p,false); throw e; }
    }
    private synchronized void complete(Permit p, boolean success) {
        if (p.generation != generation) return;
        if (p.probe) {
            probeActive=false; failures=0;
            state=success ? State.CLOSED : State.OPEN;
            openedAt=clock.getAsLong(); generation++;
        } else if (success) failures=0;
        else if (++failures >= threshold) {
            state=State.OPEN; openedAt=clock.getAsLong(); generation++;
        }
    }
    public synchronized State state() { return probeActive ? State.HALF_OPEN : state; }
}
