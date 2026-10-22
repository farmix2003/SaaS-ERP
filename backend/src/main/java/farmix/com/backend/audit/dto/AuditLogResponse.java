package farmix.com.backend.audit.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long companyId,
        Long userId,
        String userEmail,
        String action,
        String entityName,
        Long entityId,
        String oldValue,
        String newValue,
        LocalDateTime createdAt
) {
}
