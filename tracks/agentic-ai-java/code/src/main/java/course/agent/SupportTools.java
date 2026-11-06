package course.agent;

import java.util.*;

/** Synthetic records. Trusted context filters all reads. No payment API exists. */
public final class SupportTools {
  public record Order(String id, String tenant, String owner, String status, int paidCents) {}

  private final Map<String, Order> orders =
      Map.of(
          "A100",
          new Order("A100", "red", "alice", "DELAYED", 4900),
          "B200",
          new Order("B200", "blue", "bob", "DELIVERED", 2500));

  public Map<String, Agent.Tool> registry() {
    return Map.of("lookupOrder", this::lookup, "searchPolicy", this::policy);
  }

  private String lookup(Agent.Context c, Map<String, String> args) {
    requireOnly(args, "orderId");
    Order o = orders.get(args.get("orderId"));
    if (o == null || !o.tenant().equals(c.tenant()) || !o.owner().equals(c.user()))
      return "Order unavailable for this account";
    return "source=orders/" + o.id() + "; status=" + o.status() + "; paidCents=" + o.paidCents();
  }

  private String policy(Agent.Context c, Map<String, String> args) {
    requireOnly(args, "query");
    return "source=policy/v1; Delayed orders require a support review. This policy does not"
               + " authorize a refund. A human must review the exact proposal.";
  }

  private static void requireOnly(Map<String, String> args, String key) {
    if (!args.keySet().equals(Set.of(key)) || args.get(key).isBlank())
      throw new IllegalArgumentException("expected " + key + " only");
  }
}
