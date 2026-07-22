package farmix.com.backend.customer.repository;

import farmix.com.backend.customer.entity.Customer;
import farmix.com.backend.customer.entity.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository  extends JpaRepository<Customer, Long> {

    Optional<Customer> findByIdAndCompany_Id(Long id, Long companyId);

    @Query("""
            SELECT c
            FROM Customer c
            WHERE c.company.id = :companyId
              AND (:status IS NULL OR c.status = :status)
              AND (
                    :q IS NULL
                    OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%'))
                  )
            """)
    Page<Customer> search(
            @Param("companyId") Long companyId,
            @Param("q") String q,
            @Param("status") CustomerStatus status,
            Pageable pageable
    );
}
