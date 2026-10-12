package farmix.com.backend.user.dto;

import farmix.com.backend.user.entity.UserRole;
import farmix.com.backend.user.entity.UserStatus;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        Long companyId,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
