package course.agent;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;

class AgentTest {
  final Agent.Context alice = new Agent.Context("red", "alice");

  Agent agent(Agent.Model m) {
    return new Agent(m, new SupportTools().registry(), 3, Clock.systemUTC(), Duration.ofSeconds(2));
  }

  @Test
  void loopUsesToolsThenFinishes() throws Exception {
    var steps =
        new ArrayDeque<>(
            List.of(
                Agent.Decision.tool("lookupOrder", Map.of("orderId", "A100")),
                Agent.Decision.finish("delayed")));
    var r = agent(x -> steps.remove()).run(alice, "status?");
    assertEquals(2, r.modelCalls());
    assertTrue(r.trace().getLast().text().contains("DELAYED"));
  }

  @Test
  void loopIsBounded() {
    assertThrows(
        IllegalStateException.class,
        () ->
            agent(x -> Agent.Decision.tool("lookupOrder", Map.of("orderId", "A100")))
                .run(alice, "status"));
  }

  @Test
  void arbitraryToolsAreRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> agent(x -> Agent.Decision.tool("shell", Map.of())).run(alice, "status"));
  }

  @Test
  void modelCannotChooseTenant() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            agent(
                    x ->
                        Agent.Decision.tool(
                            "lookupOrder", Map.of("orderId", "B200", "tenant", "blue")))
                .run(alice, "status"));
  }

  @Test
  void crossTenantOrderIsHidden() {
    assertEquals(
        "Order unavailable for this account",
        new SupportTools().registry().get("lookupOrder").execute(alice, Map.of("orderId", "B200")));
  }

  @Test
  void sameTenantOtherUserIsHidden() {
    assertEquals(
        "Order unavailable for this account",
        new SupportTools()
            .registry()
            .get("lookupOrder")
            .execute(new Agent.Context("red", "mallory"), Map.of("orderId", "A100")));
  }

  @Test
  void oversizedQuestionRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> agent(x -> Agent.Decision.finish("ok")).run(alice, "x".repeat(4001)));
  }

  @Test
  void oversizedToolResultRejected() {
    var a =
        new Agent(
            x -> Agent.Decision.tool("huge", Map.of()),
            Map.of("huge", (c, p) -> "x".repeat(8001)),
            3,
            Clock.systemUTC(),
            Duration.ofSeconds(2));
    assertThrows(IllegalStateException.class, () -> a.run(alice, "go"));
  }

  @Test
  void approvalBindsExactProposal() {
    var l = new ApprovalLedger(Clock.systemUTC());
    var p = l.propose(alice, "A100", 1000);
    assertThrows(
        SecurityException.class,
        () ->
            l.approve(
                alice,
                new ApprovalLedger.Proposal(p.id(), p.owner(), p.orderId(), 2000, p.expires())));
    assertThrows(SecurityException.class, () -> l.approve(new Agent.Context("blue", "bob"), p));
  }

  @Test
  void repeatedApprovalHasOneEffect() throws Exception {
    var l = new ApprovalLedger(Clock.systemUTC());
    var p = l.propose(alice, "A100", 1000);
    try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
      var tasks = new ArrayList<Callable<String>>();
      for (int i = 0; i < 100; i++) tasks.add(() -> l.approve(alice, p));
      var receipts = new HashSet<String>();
      for (var f : pool.invokeAll(tasks)) receipts.add(f.get());
      assertEquals(1, receipts.size());
      assertEquals(1, l.decisions());
    }
  }

  @Test
  void expiredApprovalRejected() {
    var now = Instant.parse("2026-01-01T00:00:00Z");
    var clock =
        new Clock() {
          int n = 0;

          public ZoneId getZone() {
            return ZoneOffset.UTC;
          }

          public Clock withZone(ZoneId z) {
            return this;
          }

          public Instant instant() {
            return now.plusSeconds(n++ == 0 ? 0 : 301);
          }
        };
    var l = new ApprovalLedger(clock);
    var p = l.propose(alice, "A100", 500);
    assertThrows(IllegalStateException.class, () -> l.approve(alice, p));
  }

  @Test
  void retrievalFiltersBeforeRanking() {
    var r =
        new Retriever(
            List.of(
                new Retriever.Chunk("a", "red", "delivery delayed"),
                new Retriever.Chunk("b", "blue", "delivery delayed refund")));
    assertEquals(
        List.of("a"),
        r.search("red", "delivery refund", 4).stream().map(h -> h.chunk().id()).toList());
    assertTrue(r.search("red", "unmatched", 2).isEmpty());
  }

  @Test
  void cosineHandlesBoundaries() {
    assertEquals(1, Retriever.cosine(new double[] {1, 0}, new double[] {2, 0}), 1e-10);
    assertEquals(0, Retriever.cosine(new double[] {0, 0}, new double[] {1, 1}));
    assertThrows(
        IllegalArgumentException.class,
        () -> Retriever.cosine(new double[] {1}, new double[] {1, 2}));
  }

  @Test
  void ollamaAdapterParsesRealHttp() throws Exception {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/api/chat",
        e -> {
          String request = new String(e.getRequestBody().readAllBytes());
          assertTrue(request.contains("\"stream\":false"));
          byte[] b =
              "{\"message\":{\"content\":\"{\\\"type\\\":\\\"final\\\",\\\"answer\\\":\\\"Hello\\\"}\"}}"
                  .getBytes();
          e.sendResponseHeaders(200, b.length);
          e.getResponseBody().write(b);
          e.close();
        });
    server.start();
    try {
      var m =
          new OllamaModel(
              URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/chat"),
              "fixture",
              Duration.ofSeconds(2));
      assertEquals("Hello", m.next(List.of()).answer());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void ollamaHttpFailureIsNotAnAnswer() throws Exception {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/api/chat",
        e -> {
          e.sendResponseHeaders(503, -1);
          e.close();
        });
    server.start();
    try {
      var m =
          new OllamaModel(
              URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/chat"),
              "fixture",
              Duration.ofSeconds(1));
      assertThrows(IllegalStateException.class, () -> m.next(List.of()));
    } finally {
      server.stop(0);
    }
  }

  @Test
  void remoteModelEndpointIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new OllamaModel(URI.create("http://example.com/api/chat"), "x", Duration.ofSeconds(1)));
  }
}
