package farmix.com.backend.product.repository;

import farmix.com.backend.product.entity.Product;
import farmix.com.backend.product.entity.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Product p
        WHERE p.id = :id
          AND p.company.id = :companyId
        """)
    Optional<Product> findByIdAndCompanyIdForUpdate(
            @Param("id") Long id,
            @Param("companyId") Long companyId
    );
    Optional<Product> findByIdAndCompany_Id(Long id, Long companyId);

    boolean existsByCompany_IdAndSkuIgnoreCase(Long companyId, String sku);

    boolean existsByCompany_IdAndSkuIgnoreCaseAndIdNot(
            Long companyId,
            String sku,
            Long id
    );

    @Query("SELECT p FROM Product p WHERE p.company.id = :companyId " +
            "AND (:status IS NULL OR p.status =:status) " +
            "AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Product> search(@Param("companyId") Long companyId, @Param("q") String q, @Param("status")ProductStatus status, Pageable pageable);

}
