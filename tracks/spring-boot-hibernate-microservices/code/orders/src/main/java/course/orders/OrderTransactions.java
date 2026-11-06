package course.orders;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderTransactions {
  private final Orders orders;
  private final Events events;

  OrderTransactions(Orders o, Events e) {
    orders = o;
    events = e;
  }

  @Transactional
  public PurchaseOrder create(String key, long product, int quantity) {
    var existing = orders.findByRequestKey(key);
    if (existing.isPresent()) {
      var o = existing.get();
      if (o.productId != product || o.quantity != quantity)
        throw new Conflict("key reused with different request");
      return o;
    }
    return orders.saveAndFlush(
        new PurchaseOrder(UUID.randomUUID().toString(), key, product, quantity));
  }

  @Transactional
  public PurchaseOrder complete(String id, String status) {
    var order = orders.lock(id).orElseThrow(() -> new Missing("order not found"));
    if (!order.status.equals("PENDING")) return order;
    if (!Set.of("CONFIRMED", "REJECTED").contains(status))
      throw new IllegalArgumentException("invalid terminal status");
    order.status = status;
    events.save(new OutboxEvent(order));
    return order;
  }

  public PurchaseOrder get(String id) {
    return orders.findById(id).orElseThrow(() -> new Missing("order not found"));
  }
}
