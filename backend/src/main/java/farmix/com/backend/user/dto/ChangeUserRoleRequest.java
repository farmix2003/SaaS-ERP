package farmix.com.backend.user.dto;

import farmix.com.backend.user.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequest(

        @NotNull(message = "Role must not be null") UserRole role

) {
}
