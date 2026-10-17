package farmix.com.backend.order.dto;

import farmix.com.backend.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long companyId,
        Long customerId,
        String customerName,
        OrderStatus status,
        BigDecimal totalAmount,
        String notes,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
