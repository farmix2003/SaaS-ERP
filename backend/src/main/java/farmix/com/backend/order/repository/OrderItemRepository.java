package farmix.com.backend.order.repository;

import farmix.com.backend.analytics.dto.TopProductResponse;
import farmix.com.backend.order.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
    SELECT new farmix.com.backend.analytics.dto.TopProductResponse(
        p.id,
        p.name,
        p.sku,
        SUM(oi.quantity),
        SUM(oi.subtotal)
    )
    FROM OrderItem oi
    JOIN oi.order o
    JOIN oi.product p
    WHERE o.company.id = :companyId
      AND o.status != farmix.com.backend.order.entity.OrderStatus.CANCELLED
    GROUP BY p.id, p.name, p.sku
    ORDER BY SUM(oi.quantity) DESC
""")
    List<TopProductResponse> findTopProducts(@Param("companyId") Long companyId, Pageable pageable);

}
