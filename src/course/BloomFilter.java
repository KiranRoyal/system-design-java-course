package course;
import java.util.BitSet;

/** Approximate membership. False positives are possible; deletion is unsupported. */
public final class BloomFilter {
    private final BitSet bits; private final int size, hashes;
    public BloomFilter(int size,int hashes) {
        if(size<1 || hashes<1) throw new IllegalArgumentException();
        this.size=size;this.hashes=hashes;bits=new BitSet(size);
    }
    private int position(String key,int n) { return (int)Math.floorMod(HashRing.hash(n+":"+key),(long)size); }
    public synchronized void add(String key) { for(int i=0;i<hashes;i++) bits.set(position(key,i)); }
    public synchronized boolean mightContain(String key) {
        for(int i=0;i<hashes;i++) if(!bits.get(position(key,i))) return false;
        return true;
    }
}
