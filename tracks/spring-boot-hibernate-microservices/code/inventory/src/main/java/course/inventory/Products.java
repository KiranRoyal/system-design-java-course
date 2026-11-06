package course.inventory;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface Products extends JpaRepository<Product, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id=:id")
  Optional<Product> lock(@Param("id") long id);
}
