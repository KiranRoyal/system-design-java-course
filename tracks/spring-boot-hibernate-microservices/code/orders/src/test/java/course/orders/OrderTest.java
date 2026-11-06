package course.orders;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;

@SpringBootTest
class OrderTest {
  static final AtomicInteger status = new AtomicInteger(200), calls = new AtomicInteger();
  static volatile String observedBody, observedKey;
  static final HttpServer server;

  static {
    try {
      server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.createContext(
          "/reservations",
          e -> {
            calls.incrementAndGet();
            observedBody = new String(e.getRequestBody().readAllBytes());
            observedKey = e.getRequestHeaders().getFirst("Idempotency-Key");
            if (status.get() == 598) {
              try {
                Thread.sleep(2300);
              } catch (InterruptedException x) {
                Thread.currentThread().interrupt();
              }
              e.close();
              return;
            }
            e.sendResponseHeaders(status.get(), -1);
            e.close();
          });
      server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("inventory.base-url", () -> "http://127.0.0.1:" + server.getAddress().getPort());
  }

  @Autowired OrderService service;
  @Autowired OrderTransactions tx;
  @Autowired Orders orders;
  @Autowired Events events;

  @BeforeEach
  void reset() {
    events.deleteAll();
    orders.deleteAll();
    status.set(200);
    calls.set(0);
  }

  @AfterAll
  static void stop() {
    server.stop(0);
  }

  @Test
  void confirmsAndRecordsOutboxAtomically() {
    var o = service.place("a", 1, 2);
    assertEquals("CONFIRMED", o.status);
    assertEquals(1, events.count());
    assertEquals(o.id, observedKey);
    assertTrue(observedBody.contains("\"quantity\":2"));
  }

  @Test
  void sameIntentReplaysWithoutNewHttpCall() {
    var first = service.place("a", 1, 2);
    var second = service.place("a", 1, 2);
    assertEquals(first.id, second.id);
    assertEquals(1, calls.get());
    assertEquals(1, events.count());
  }

  @Test
  void conflictingIntentRejected() {
    service.place("a", 1, 2);
    assertThrows(Conflict.class, () -> service.place("a", 1, 3));
  }

  @Test
  void stockRejectionIsTerminal() {
    status.set(409);
    assertEquals("REJECTED", service.place("a", 1, 2).status);
    assertEquals(1, events.count());
  }

  @Test
  void outageRemainsPendingThenReconciles() {
    status.set(503);
    var o = service.place("a", 1, 2);
    assertEquals("PENDING", o.status);
    assertEquals(0, events.count());
    status.set(200);
    assertEquals(o.id, service.place("a", 1, 2).id);
    assertEquals("CONFIRMED", tx.get(o.id).status);
    assertEquals(1, events.count());
  }

  @Test
  void timeoutIsUnknownNotRejection() {
    status.set(598);
    assertEquals("PENDING", service.place("a", 1, 2).status);
    assertEquals(0, events.count());
  }

  @Test
  void repeatedCompletionEmitsOnce() {
    var o = tx.create("a", 1, 2);
    tx.complete(o.id, "CONFIRMED");
    tx.complete(o.id, "CONFIRMED");
    assertEquals(1, events.count());
  }

  @Test
  void badInputNeverCallsInventory() {
    assertThrows(IllegalArgumentException.class, () -> service.place("a", 1, 0));
    assertEquals(0, calls.get());
  }

  @Test
  void invalidTransitionRollsBack() {
    var o = tx.create("a", 1, 1);
    assertThrows(IllegalArgumentException.class, () -> tx.complete(o.id, "BOGUS"));
    assertEquals("PENDING", tx.get(o.id).status);
    assertEquals(0, events.count());
  }
}
