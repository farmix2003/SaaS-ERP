package farmix.com.backend.audit.repository;

import farmix.com.backend.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
    SELECT a FROM AuditLog a
    LEFT JOIN a.user u
    WHERE a.company.id = :companyId
      AND (:action IS NULL OR a.action = :action)
      AND (:entityName IS NULL OR a.entityName = :entityName)
      AND (:userId IS NULL OR (u IS NOT NULL AND u.id = :userId))
""")
   Page<AuditLog> search(@Param("companyId")Long companyId,
                         @Param("action") String action,
                         @Param("entityName") String entityName,
                         @Param("userId") Long userId,
                         Pageable pageable
                         );

}
