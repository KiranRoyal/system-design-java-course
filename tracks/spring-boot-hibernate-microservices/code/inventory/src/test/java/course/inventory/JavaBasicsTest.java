package course.inventory;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;

class JavaBasicsTest {
  @Test
  void decimalMoneyTotalsExactly() {
    assertEquals(
        new BigDecimal("0.30"),
        JavaBasics.total(List.of(new JavaBasics.Line("a", 3, new BigDecimal("0.10")))));
  }

  @Test
  void duplicateSkuQuantitiesAreGrouped() {
    assertEquals(
        Map.of("a", 5),
        JavaBasics.quantities(
            List.of(
                new JavaBasics.Line("a", 2, BigDecimal.ONE),
                new JavaBasics.Line("a", 3, BigDecimal.ONE))));
  }

  @Test
  void invalidLineRejected() {
    assertThrows(IllegalArgumentException.class, () -> new JavaBasics.Line("a", 0, BigDecimal.ONE));
  }
}
