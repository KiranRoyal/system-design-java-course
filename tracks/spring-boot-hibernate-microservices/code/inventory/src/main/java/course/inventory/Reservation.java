package course.inventory;

import jakarta.persistence.*;

@Entity
@Table(name = "reservations")
class Reservation {
  @Id
  @Column(name = "request_key")
  String requestKey;

  @Column(name = "product_id")
  long productId;

  int quantity;

  protected Reservation() {}

  Reservation(String key, long product, int quantity) {
    this.requestKey = key;
    this.productId = product;
    this.quantity = quantity;
  }

  boolean matches(long p, int q) {
    return productId == p && quantity == q;
  }
}
