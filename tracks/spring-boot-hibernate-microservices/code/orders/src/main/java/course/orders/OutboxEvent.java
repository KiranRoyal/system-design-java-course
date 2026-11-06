package course.orders;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "outbox_events")
class OutboxEvent {
  @Id String id;

  @Column(name = "order_id")
  String orderId;

  @Column(name = "event_type")
  String eventType;

  String payload;

  @Column(name = "created_at")
  Instant createdAt;

  protected OutboxEvent() {}

  OutboxEvent(PurchaseOrder order) {
    id = UUID.randomUUID().toString();
    orderId = order.id;
    eventType = "Order" + order.status;
    payload = "orderId=" + order.id + ";status=" + order.status;
    createdAt = Instant.now();
  }
}
