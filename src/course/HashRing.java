package course;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** Placement demonstration only. It does not migrate data, replicate it or detect failed nodes. */
public final class HashRing {
    private final NavigableMap<Long,String> ring = new TreeMap<>();
    private final Set<String> nodes = new HashSet<>();
    private final int virtualNodes;
    public HashRing(int virtualNodes) {
        if (virtualNodes < 1) throw new IllegalArgumentException();
        this.virtualNodes = virtualNodes;
    }
    static long hash(String key) {
        try {
            return ByteBuffer.wrap(MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8))).getLong();
        } catch (NoSuchAlgorithmException e) { throw new AssertionError(e); }
    }
    public synchronized void add(String node) {
        Objects.requireNonNull(node);
        if (nodes.contains(node)) return;
        Map<Long,String> additions = new HashMap<>();
        for (int i=0; i<virtualNodes; i++) {
            long h = hash(node + "#" + i);
            if (ring.containsKey(h) || additions.put(h,node) != null)
                throw new IllegalStateException("Hash collision: choose another node name");
        }
        ring.putAll(additions); nodes.add(node);
    }
    public synchronized void remove(String node) {
        ring.values().removeIf(node::equals); nodes.remove(node);
    }
    public synchronized String owner(String key) {
        if (ring.isEmpty()) throw new IllegalStateException("No nodes");
        var e = ring.ceilingEntry(hash(key));
        return (e == null ? ring.firstEntry() : e).getValue();
    }
}
