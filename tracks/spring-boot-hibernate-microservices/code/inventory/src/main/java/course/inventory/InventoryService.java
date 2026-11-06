package course.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
  private final Products products;
  private final Reservations reservations;

  public InventoryService(Products p, Reservations r) {
    products = p;
    reservations = r;
  }

  public record Reserved(String key, long productId, int quantity, int remaining) {}

  @Transactional
  public Reserved reserve(String key, long id, int quantity) {
    if (key == null || !key.matches("[A-Za-z0-9_-]{1,80}") || quantity < 1 || quantity > 1000)
      throw new IllegalArgumentException("invalid key or quantity");
    // All requests for this product serialize at its database row. The key has a unique constraint
    // too.
    var product = products.lock(id).orElseThrow(() -> new Missing("product not found"));
    var previous = reservations.findById(key);
    if (previous.isPresent()) {
      if (!previous.get().matches(id, quantity))
        throw new Conflict("key reused with different request");
      return new Reserved(key, id, quantity, product.getStock());
    }
    product.reserve(quantity);
    reservations.saveAndFlush(new Reservation(key, id, quantity));
    return new Reserved(key, id, quantity, product.getStock());
  }
}
