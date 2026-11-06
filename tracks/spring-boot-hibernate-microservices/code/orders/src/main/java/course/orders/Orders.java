package course.orders;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface Orders extends JpaRepository<PurchaseOrder, String> {
  Optional<PurchaseOrder> findByRequestKey(String key);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from PurchaseOrder o where o.id=:id")
  Optional<PurchaseOrder> lock(@Param("id") String id);
}
