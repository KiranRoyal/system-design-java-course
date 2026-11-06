package course.inventory;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class InventoryController {
  private final Products products;
  private final InventoryService service;

  InventoryController(Products p, InventoryService s) {
    products = p;
    service = s;
  }

  public record ReserveRequest(@Positive long productId, @Min(1) @Max(1000) int quantity) {}

  public record ProductView(long id, String name, int stock, long version) {}

  @GetMapping("/products")
  public java.util.List<ProductView> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    if (page < 0 || size < 1 || size > 100)
      throw new IllegalArgumentException("page >= 0 and size 1 to 100");
    return products.findAll(PageRequest.of(page, size, Sort.by("id"))).stream()
        .map(p -> new ProductView(p.getId(), p.getName(), p.getStock(), p.getVersion()))
        .toList();
  }

  @PostMapping("/reservations")
  public InventoryService.Reserved reserve(
      @RequestHeader("Idempotency-Key") String key, @Valid @RequestBody ReserveRequest body) {
    return service.reserve(key, body.productId(), body.quantity());
  }
}
