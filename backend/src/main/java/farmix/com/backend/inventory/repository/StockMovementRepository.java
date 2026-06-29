package farmix.com.backend.inventory.repository;

import farmix.com.backend.inventory.entity.StockMovement;
import farmix.com.backend.inventory.entity.StockMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository  extends JpaRepository<StockMovement, Long> {


    @Query("""
            SELECT sm
            FROM StockMovement sm
            JOIN FETCH sm.product p
            WHERE sm.company.id = :companyId
              AND (:productId IS NULL OR p.id = :productId)
              AND (:type IS NULL OR sm.type = :type)
            """)
    Page<StockMovement> search(
            @Param("companyId") Long companyId,
            @Param("productId") Long productId,
            @Param("type") StockMovementType type,
            Pageable pageable
    );
}
