package farmix.com.backend.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockOutRequest(
        @NotNull(message = "Product id is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than zero")
        Integer quantity,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
