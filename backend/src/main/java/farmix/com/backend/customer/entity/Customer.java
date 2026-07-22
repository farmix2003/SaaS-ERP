package farmix.com.backend.customer.entity;

import farmix.com.backend.company.entity.Company;
import farmix.com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "customers",
        indexes = {
                @Index(name = "idx_customers_company_id", columnList = "company_id"),
                @Index(name = "idx_customers_company_status", columnList = "company_id,status"),
                @Index(name = "idx_customers_company_full_name", columnList = "company_id,full_name"),
                @Index(name = "idx_customers_company_phone", columnList = "company_id,phone"),
                @Index(name = "idx_customers_company_email", columnList = "company_id,email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "company_id", nullable = false)
        private Company company;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "created_by", nullable = false)
        private User createdBy;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "updated_by")
        private User updatedBy;

        @Column(name = "full_name", nullable = false, length = 150)
        private String fullName;

        @Column(length = 30)
        private String phone;

        @Column(length = 150)
        private String email;

        @Column(length = 500)
        private String address;

        @Column(length = 1000)
        private String notes;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 30)
        private CustomerStatus status;

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        @PrePersist
        void onCreate() {
                this.createdAt = LocalDateTime.now();

                if (this.status == null) {
                        this.status = CustomerStatus.ACTIVE;
                }
        }

        @PreUpdate
        void onUpdate() {
                this.updatedAt = LocalDateTime.now();
        }
}