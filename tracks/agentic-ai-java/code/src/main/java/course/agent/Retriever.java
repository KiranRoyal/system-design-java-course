package course.agent;

import java.util.*;
import java.util.stream.*;

/** Lexical baseline for teaching evaluation. This is not a vector embedding model. */
public final class Retriever {
  public record Chunk(String id, String tenant, String text) {}

  public record Hit(Chunk chunk, double score) {}

  private final List<Chunk> chunks;

  public Retriever(List<Chunk> chunks) {
    this.chunks = List.copyOf(chunks);
  }

  private static Set<String> words(String s) {
    return Arrays.stream(s.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
        .filter(x -> !x.isBlank())
        .collect(Collectors.toSet());
  }

  public List<Hit> search(String tenant, String query, int k) {
    if (k < 1 || k > 20) throw new IllegalArgumentException("k out of range");
    var q = words(query);
    return chunks.stream()
        .filter(c -> c.tenant().equals(tenant))
        .map(
            c -> {
              var w = words(c.text());
              long overlap = q.stream().filter(w::contains).count();
              return new Hit(c, q.isEmpty() ? 0 : (double) overlap / q.size());
            })
        .filter(h -> h.score() > 0)
        .sorted(
            Comparator.comparingDouble(Hit::score).reversed().thenComparing(h -> h.chunk().id()))
        .limit(k)
        .toList();
  }

  public static double cosine(double[] a, double[] b) {
    if (a.length == 0 || a.length != b.length) throw new IllegalArgumentException("dimensions");
    double dot = 0, aa = 0, bb = 0;
    for (int i = 0; i < a.length; i++) {
      if (!Double.isFinite(a[i]) || !Double.isFinite(b[i]))
        throw new IllegalArgumentException("finite vectors required");
      dot += a[i] * b[i];
      aa += a[i] * a[i];
      bb += b[i] * b[i];
    }
    return aa == 0 || bb == 0 ? 0 : dot / (Math.sqrt(aa) * Math.sqrt(bb));
  }
}
