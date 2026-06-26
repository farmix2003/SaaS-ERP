package farmix.com.backend.common;

import farmix.com.backend.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSecureController {

    @GetMapping("/api/v1/me")
    public String me(@AuthenticationPrincipal UserPrincipal user) {
        return "User: " + user.getEmail()
                + ", companyId: " + user.getCompanyId()
                + ", role: " + user.getRole();
    }

    @GetMapping("/api/v1/admin-only")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public String adminOnly() {
        return "Only COMPANY_ADMIN can access this";
    }
}