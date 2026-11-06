package course.inventory;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class InventoryTest {
  @Autowired InventoryService service;
  @Autowired Products products;
  @Autowired Reservations reservations;
  @Autowired PlatformTransactionManager tm;
  @PersistenceContext EntityManager em;

  @BeforeEach
  void reset() {
    reservations.deleteAll();
    products.deleteAll();
    products.saveAndFlush(new Product(1, "Workbook", 10));
  }

  @Test
  void duplicateReservationDoesNotConsumeTwice() {
    service.reserve("a", 1, 3);
    service.reserve("a", 1, 3);
    assertEquals(7, products.findById(1L).orElseThrow().getStock());
    assertEquals(1, reservations.count());
  }

  @Test
  void conflictingKeyRollsBack() {
    service.reserve("a", 1, 3);
    assertThrows(Conflict.class, () -> service.reserve("a", 1, 4));
    assertEquals(7, products.findById(1L).orElseThrow().getStock());
  }

  @Test
  void insufficientStockRollsBack() {
    assertThrows(Conflict.class, () -> service.reserve("a", 1, 11));
    assertEquals(10, products.findById(1L).orElseThrow().getStock());
    assertEquals(0, reservations.count());
  }

  @Test
  void concurrentBuyersCannotOversell() throws Exception {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var tasks = new ArrayList<Callable<Boolean>>();
      for (int i = 0; i < 20; i++) {
        String key = "k" + i;
        tasks.add(
            () -> {
              try {
                service.reserve(key, 1, 1);
                return true;
              } catch (Conflict e) {
                return false;
              }
            });
      }
      int success = 0;
      for (var f : executor.invokeAll(tasks)) if (f.get()) success++;
      assertEquals(10, success);
      assertEquals(0, products.findById(1L).orElseThrow().getStock());
      assertEquals(10, reservations.count());
    }
  }

  @Test
  void duplicateConcurrentRequestsHaveOneEffect() throws Exception {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var tasks = new ArrayList<Callable<Object>>();
      for (int i = 0; i < 20; i++) tasks.add(() -> service.reserve("same", 1, 2));
      for (var f : executor.invokeAll(tasks)) f.get();
      assertEquals(8, products.findById(1L).orElseThrow().getStock());
    }
  }

  @Test
  void missingProductDoesNotCreateReservation() {
    assertThrows(Missing.class, () -> service.reserve("a", 999, 1));
    assertEquals(0, reservations.count());
  }

  @Test
  void invalidQuantityRejected() {
    assertThrows(IllegalArgumentException.class, () -> service.reserve("a", 1, 0));
  }

  @Test
  void staleEntityVersionRejected() {
    var stale = products.findById(1L).orElseThrow();
    var fresh = products.findById(1L).orElseThrow();
    fresh.rename("New");
    products.saveAndFlush(fresh);
    stale.rename("Old");
    assertThrows(
        org.springframework.orm.ObjectOptimisticLockingFailureException.class,
        () -> products.saveAndFlush(stale));
  }

  @Test
  void explicitRollbackDiscardsDirtyChanges() {
    var t = new TransactionTemplate(tm);
    assertThrows(
        IllegalStateException.class,
        () ->
            t.execute(
                s -> {
                  var p = em.find(Product.class, 1L);
                  p.rename("Should roll back");
                  throw new IllegalStateException("fail");
                }));
    assertEquals("Workbook", products.findById(1L).orElseThrow().getName());
  }
}
