package course.inventory;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
  @Id private Long id;
  private String name;
  private int stock;
  @Version private long version;

  protected Product() {}

  public Product(long id, String name, int stock) {
    this.id = id;
    this.name = name;
    this.stock = stock;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getStock() {
    return stock;
  }

  public long getVersion() {
    return version;
  }

  void reserve(int quantity) {
    if (quantity > stock) throw new Conflict("insufficient stock");
    stock -= quantity;
  }

  public void rename(String name) {
    this.name = name;
  }
}
