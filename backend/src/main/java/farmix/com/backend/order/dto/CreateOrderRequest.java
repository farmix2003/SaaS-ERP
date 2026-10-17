package farmix.com.backend.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "Customer ID is required")
        Long customerId,
        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid CreateOrderItemRequest> items,
        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        String notes
) {
}
