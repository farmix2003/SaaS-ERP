package farmix.com.backend.user.service;

import farmix.com.backend.audit.service.AuditLogService;
import farmix.com.backend.common.exception.BadRequestException;
import farmix.com.backend.common.exception.NotFoundException;
import farmix.com.backend.company.entity.Company;
import farmix.com.backend.company.repository.CompanyRepository;
import farmix.com.backend.security.CurrentUser;
import farmix.com.backend.user.dto.ChangeUserRoleRequest;
import farmix.com.backend.user.dto.CreateUserRequest;
import farmix.com.backend.user.dto.UpdateUserRequest;
import farmix.com.backend.user.dto.UserResponse;
import farmix.com.backend.user.entity.User;
import farmix.com.backend.user.entity.UserRole;
import farmix.com.backend.user.entity.UserStatus;
import farmix.com.backend.user.mapper.UserMapper;
import farmix.com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CurrentUser currentUser;
    private final AuditLogService auditLogService;

    @Transactional
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public UserResponse create(CreateUserRequest createUserRequest) {
        Long companyId = currentUser.getCompanyId();

        if(createUserRequest.role() == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Cannot create a user with role SUPER_ADMIN");
        }

        String email = createUserRequest.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }

        Company company = companyRepository.getReferenceById(companyId);

        User user = User.builder()
                .company(company)
                .firstName(createUserRequest.firstName())
                .lastName(createUserRequest.lastName())
                .email(email)
                .password(passwordEncoder.encode(createUserRequest.password()))
                .role(createUserRequest.role())
                .status(UserStatus.ACTIVE)
                .build();
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public Page<UserResponse> list(String q, UserRole role, UserStatus status, Pageable pageable) {
     Long companyId = currentUser.getCompanyId();
     Pageable safePageable = sanitizePageable(pageable);
     return userRepository.search(companyId, normalizeQuery(q), role, status, safePageable)
             .map(userMapper::toResponse);

}

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public UserResponse getById(Long id) {
        Long companyId = currentUser.getCompanyId();
        User user = userRepository.findByIdAndCompany_Id(id, companyId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public UserResponse update(Long id, UpdateUserRequest req){
        Long companyId = currentUser.getCompanyId();
        User user = userRepository.findByIdAndCompany_Id(id, companyId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public UserResponse changeRole(Long id, ChangeUserRoleRequest req){
        Long companyId = currentUser.getCompanyId();
        Long currentUserId = currentUser.getUserId();
        if(id.equals(currentUserId)) {
            throw new BadRequestException("You can not change your own role");
        }
        if(req.role() == UserRole.SUPER_ADMIN) {
            throw new BadRequestException("Cannot change role of user with role SUPER_ADMIN");
        }

                User user = userRepository.findByIdAndCompany_Id(id, companyId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        String oldRole = user.getStatus().name();

        user.setRole(req.role());
        auditLogService.record(companyId, currentUserId, "USER_ROLE_CHANGED", "User", user.getId(), oldRole, req.role().name());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public UserResponse deactivate(Long id){
        Long companyId = currentUser.getCompanyId();
        Long currentUserId = currentUser.getUserId();

        if(id.equals(currentUserId)) {
            throw new BadRequestException("You can not deactivate yourself!");
        }

        User user = userRepository.findByIdAndCompany_Id(id, companyId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setStatus(UserStatus.INACTIVE);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public UserResponse activate(Long id) {
        Long companyId = currentUser.getCompanyId();
        User user = userRepository.findByIdAndCompany_Id(id, companyId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        return userMapper.toResponse(user);
    }

    private String normalizeQuery(String q) {
        return (q == null || q.isBlank()) ? null : q.trim();
    }

    private Pageable sanitizePageable(Pageable pageable) {
       int page = Math.max(0, pageable.getPageNumber());
       int size = Math.min(Math.max(1, pageable.getPageSize()), 100);
       return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

}
