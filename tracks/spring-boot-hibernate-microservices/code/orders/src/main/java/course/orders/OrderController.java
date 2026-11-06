package course.orders;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
class OrderController {
  private final OrderService service;
  private final OrderTransactions tx;

  OrderController(OrderService s, OrderTransactions tx) {
    service = s;
    this.tx = tx;
  }

  record Request(@Positive long productId, @Min(1) @Max(1000) int quantity) {}

  record View(String id, long productId, int quantity, String status) {
    static View of(PurchaseOrder o) {
      return new View(o.id, o.productId, o.quantity, o.status);
    }
  }

  @PostMapping("/orders")
  ResponseEntity<View> place(
      @RequestHeader("Idempotency-Key") String key, @Valid @RequestBody Request request) {
    var o = service.place(key, request.productId(), request.quantity());
    return ResponseEntity.status(o.status.equals("PENDING") ? 202 : 200).body(View.of(o));
  }

  @GetMapping("/orders/{id}")
  View get(@PathVariable String id) {
    return View.of(tx.get(id));
  }
}
