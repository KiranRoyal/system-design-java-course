package course.agent;

import java.time.*;
import java.util.*;

/** Human-facing demo boundary, deliberately NOT registered as a model tool. In-memory only. */
public final class ApprovalLedger {
  public record Proposal(
      UUID id, Agent.Context owner, String orderId, int cents, Instant expires) {}

  private final Map<UUID, Proposal> pending = new HashMap<>();
  private final Map<UUID, String> receipts = new HashMap<>();
  private final Clock clock;

  public ApprovalLedger(Clock clock) {
    this.clock = clock;
  }

  public synchronized Proposal propose(Agent.Context c, String orderId, int cents) {
    if (orderId == null || orderId.isBlank() || cents < 1 || cents > 5000)
      throw new IllegalArgumentException("invalid proposal");
    Proposal p =
        new Proposal(UUID.randomUUID(), c, orderId, cents, clock.instant().plusSeconds(300));
    pending.put(p.id(), p);
    return p;
  }

  public synchronized String approve(Agent.Context authenticatedHuman, Proposal exactProposal) {
    Proposal saved = pending.get(exactProposal.id());
    if (saved == null || !saved.equals(exactProposal) || !saved.owner().equals(authenticatedHuman))
      throw new SecurityException("approval does not match stored proposal and human");
    if (receipts.containsKey(saved.id())) return receipts.get(saved.id());
    if (!clock.instant().isBefore(saved.expires()))
      throw new IllegalStateException("proposal expired");
    // Records a simulated decision only. A real refund must recheck eligibility, amount, and use a
    // provider idempotency key.
    String receipt = "SIMULATED-APPROVAL-" + saved.id();
    receipts.put(saved.id(), receipt);
    return receipt;
  }

  public synchronized int decisions() {
    return receipts.size();
  }
}
