package course.inventory;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.*;

/** Pure domain examples, independent of Spring and Hibernate. */
public final class JavaBasics {
  public record Line(String sku, int quantity, BigDecimal unitPrice) {
    public Line {
      if (sku == null
          || sku.isBlank()
          || quantity < 1
          || unitPrice == null
          || unitPrice.signum() < 0) throw new IllegalArgumentException("invalid line");
    }

    public BigDecimal total() {
      return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
  }

  public interface Discount {
    BigDecimal apply(BigDecimal amount);
  }

  public static BigDecimal total(List<Line> lines) {
    return lines.stream().map(Line::total).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public static Map<String, Integer> quantities(List<Line> lines) {
    return lines.stream()
        .collect(Collectors.groupingBy(Line::sku, Collectors.summingInt(Line::quantity)));
  }
}
