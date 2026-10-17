package farmix.com.backend.order.repository;

import farmix.com.backend.order.entity.Order;
import farmix.com.backend.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}
