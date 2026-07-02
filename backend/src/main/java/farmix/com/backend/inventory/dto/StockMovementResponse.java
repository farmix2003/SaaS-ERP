package farmix.com.backend.inventory.dto;

import java.time.LocalDateTime;

public record StockMovementResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        String type,
        Integer quantity,
        Integer previousQuantity,
        Integer newQuantity,
        String reason,
        LocalDateTime createdAt
) {
}
