package farmix.com.backend.audit.controller;

import farmix.com.backend.audit.dto.AuditLogResponse;
import farmix.com.backend.audit.entity.AuditLog;
import farmix.com.backend.audit.service.AuditLogService;
import farmix.com.backend.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService service;

    @GetMapping
    public PageResponse<AuditLogResponse> search(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Long userId,
            Pageable pageable
    ){
        return PageResponse.from(service.search(action, entityName, userId, pageable));
    }

}
