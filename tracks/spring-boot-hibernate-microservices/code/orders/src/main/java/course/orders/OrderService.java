package course.orders;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
  private final OrderTransactions tx;
  private final InventoryClient inventory;

  public OrderService(OrderTransactions tx, InventoryClient inventory) {
    this.tx = tx;
    this.inventory = inventory;
  }

  public PurchaseOrder place(String key, long product, int quantity) {
    if (key == null
        || !key.matches("[A-Za-z0-9_-]{1,80}")
        || product < 1
        || quantity < 1
        || quantity > 1000) throw new IllegalArgumentException("invalid order request");
    PurchaseOrder order;
    try {
      order = tx.create(key, product, quantity);
    } catch (DataIntegrityViolationException race) {
      order = tx.create(key, product, quantity);
    }
    if (!order.status.equals("PENDING")) return order;
    // No database transaction is held across the HTTP request.
    return switch (inventory.reserve(order.id, product, quantity)) {
      case RESERVED -> tx.complete(order.id, "CONFIRMED");
      case REJECTED -> tx.complete(order.id, "REJECTED");
      case UNKNOWN -> tx.get(order.id);
    };
  }
}
