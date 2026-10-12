package farmix.com.backend.user.repository;

import java.util.Optional;

import farmix.com.backend.user.entity.UserRole;
import farmix.com.backend.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import farmix.com.backend.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    //Tenant-aware query to find a user by ID and company ID
    Optional<User> findByIdAndCompany_Id(Long userId, Long companyId);

    @Query("""
     SELECT u FROM User u
             WHERE u.company.id = :companyId
               AND (:role IS NULL OR u.role = :role)
               AND (:status IS NULL OR u.status = :status)
               AND (
                 :q IS NULL
                 OR LOWER(CAST(u.firstName AS text)) LIKE LOWER(CAST(CONCAT('%', :q, '%') AS text))
                 OR LOWER(CAST(u.lastName AS text)) LIKE LOWER(CAST(CONCAT('%', :q, '%') AS text))
                 OR LOWER(CAST(u.email AS text)) LIKE LOWER(CAST(CONCAT('%', :q, '%') AS text))
               )
""")
    Page<User> search(@Param("companyId") Long companyId, @Param("q") String q, @Param("role") UserRole role, @Param("status") UserStatus status, Pageable pageable);


}
