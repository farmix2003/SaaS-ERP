package farmix.com.backend.order.repository;

import farmix.com.backend.order.entity.Order;
import farmix.com.backend.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndCompany_Id(Long id, Long companyId);

    @Query("""
        SELECT o FROM Order o
        JOIN o.customer c
        WHERE o.company.id = :companyId
          AND (:status IS NULL OR o.status = :status)
          AND (:customerId IS NULL OR o.customer.id = :customerId)
    """)
    Page<Order> search(
            @Param("companyId") Long companyId,
            @Param("status") OrderStatus status,
            @Param("customerId") Long customerId,
            Pageable pageable
    );

    @Query("""
      SELECT COALESCE(SUM(o.totalAmount), 0)
      FROM Order o
      WHERE o.company.id = :companyId
      AND o.status != farmix.com.backend.order.entity.OrderStatus.CANCELLED
""")
    BigDecimal calculateTotalRevenue(@Param("companyId") Long companyId);

    @Query("""
    SELECT COALESCE(SUM(o.totalAmount), 0)
    FROM Order o
    WHERE o.company.id = :companyId
      AND o.status = farmix.com.backend.order.entity.OrderStatus.PAID
""")
    BigDecimal calculatePaidRevenue(@Param("companyId") Long companyId);
    // Count orders by status
    long countByCompany_IdAndStatus(Long companyId, OrderStatus status);
    long countByCompany_Id(Long companyId);

}
