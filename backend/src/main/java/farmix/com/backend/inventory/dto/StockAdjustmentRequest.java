package farmix.com.backend.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockAdjustmentRequest(
        @NotNull(message = "Product id is required")
        Long productId,

        @NotNull(message = "New quantity is required")
        @Min(value = 0, message = "New quantity cannot be negative")
        Integer newQuantity,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
