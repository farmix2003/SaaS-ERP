package farmix.com.backend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    /**
     * Timestamp when the entity was first created.
     * This field is automatically populated by Spring Data JPA auditing
     * and cannot be updated after initial creation.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the entity was last modified.
     * This field is automatically updated by Spring Data JPA auditing
     * whenever the entity is saved or updated.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

}
