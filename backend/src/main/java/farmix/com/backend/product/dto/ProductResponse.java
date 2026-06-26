package farmix.com.backend.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        String description,
        BigDecimal salePrice,
        BigDecimal costPrice,
        Integer stockQuantity,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
