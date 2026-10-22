package farmix.com.backend.audit.mapper;

import farmix.com.backend.audit.dto.AuditLogResponse;
import farmix.com.backend.audit.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog log){
        return new AuditLogResponse(
                log.getId(),
                log.getCompany().getId(),
                log.getUser().getId(),
                log.getUser().getEmail(),
                log.getAction(),
                log.getEntityName(),
                log.getEntityId(),
                log.getOldValue(),
                log.getNewValue(),
                log.getCreatedAt()
        );
    }

}
