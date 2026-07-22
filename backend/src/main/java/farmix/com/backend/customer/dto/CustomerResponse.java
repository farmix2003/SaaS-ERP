package farmix.com.backend.customer.dto;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String fullName,
        String phone,
        String email,
        String address,
        String notes,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
