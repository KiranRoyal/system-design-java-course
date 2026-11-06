package course.orders;

import jakarta.persistence.*;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
  @Id String id;

  @Column(name = "request_key", nullable = false, unique = true)
  String requestKey;

  @Column(name = "product_id")
  long productId;

  int quantity;
  String status;
  @Version long version;

  protected PurchaseOrder() {}

  PurchaseOrder(String id, String key, long product, int quantity) {
    this.id = id;
    this.requestKey = key;
    this.productId = product;
    this.quantity = quantity;
    this.status = "PENDING";
  }

  public String getId() {
    return id;
  }

  public String getStatus() {
    return status;
  }

  public int getQuantity() {
    return quantity;
  }

  public long getProductId() {
    return productId;
  }
}
