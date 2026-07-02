package farmix.com.backend.inventory.entity;

import farmix.com.backend.company.entity.Company;
import farmix.com.backend.product.entity.Product;
import farmix.com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "stock_movements",
        indexes = {
                @Index(name = "idx_stock_movements_company_id", columnList = "company_id"),
                @Index(name = "idx_stock_movements_product_id", columnList = "product_id"),
                @Index(name = "idx_stock_movements_company_product", columnList = "company_id,product_id"),
                @Index(name = "idx_stock_movements_company_created_at", columnList = "company_id,created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id",nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockMovementType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity;

    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
