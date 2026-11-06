package course;
import java.util.*;

/** Deterministic replica-lag model; deliberately NOT consensus or a networked database. */
public final class ReplicationDemo {
    public record Value(long version, String data) {}
    private final Map<String,Value> leader=new HashMap<>(), follower=new HashMap<>();
    private long version;
    public Value write(String key,String value) { var v=new Value(++version,value); leader.put(key,v); return v; }
    public Value readFollower(String key) { return follower.get(key); }
    public void replicate(String key,Value value) {
        follower.compute(key,(k,old)->old==null || old.version<value.version ? value : old);
    }
    public Value readYourWrites(String key,long minimumVersion) {
        var v=follower.get(key);
        return v!=null && v.version>=minimumVersion ? v : leader.get(key);
    }
}
