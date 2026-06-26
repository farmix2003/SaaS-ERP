package farmix.com.backend.security;

import farmix.com.backend.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public UserPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Object user = auth.getPrincipal();

        if (!(user instanceof UserPrincipal userPrincipal)) {
            throw new UnauthorizedException("Invalid authenticated user context");
        }
        return userPrincipal;
    }

    public Long getUserId() {
        return getPrincipal().getId();
    }

    public Long getCompanyId() {
        return getPrincipal().getCompanyId();
    }

    public String getEmail() {
        return getPrincipal().getEmail();
    }

    public String getRole() {
        return getPrincipal().getRole();
    }

    public boolean hasRole(String role) {
        return getRole().equals(role);
    }

}
