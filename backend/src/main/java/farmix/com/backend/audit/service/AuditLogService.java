package farmix.com.backend.audit.service;

import farmix.com.backend.audit.dto.AuditLogResponse;
import farmix.com.backend.audit.entity.AuditLog;
import farmix.com.backend.audit.mapper.AuditLogMapper;
import farmix.com.backend.audit.repository.AuditLogRepository;
import farmix.com.backend.company.entity.Company;
import farmix.com.backend.company.repository.CompanyRepository;
import farmix.com.backend.security.CurrentUser;
import farmix.com.backend.user.entity.User;
import farmix.com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper mapper;
    private final CurrentUser currentUser;

    @Transactional(propagation = Propagation.REQUIRED)
    public void record(Long companyId, Long userId, String action, String entityName, Long entityId, String oldValue, String newValue){
        Company company = companyRepository.getReferenceById(companyId);
        User user = (userId != null) ? userRepository.getReferenceById(userId) : null;

        AuditLog log = AuditLog.builder()
                .company(company)
                .user(user)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public Page<AuditLogResponse> search(String action, String entityName, Long userId, Pageable pageable){
        Long companyId = currentUser.getCompanyId();
        Pageable safePageable = sanitizePageable(pageable);

        return auditLogRepository.search(companyId, action, entityName, userId, safePageable)
                .map(mapper::toResponse);
    }

    private Pageable sanitizePageable(Pageable pageable) {
        int page = Math.max(0, pageable.getPageNumber());
        int size = Math.min(Math.max(1, pageable.getPageNumber()), 100);
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
