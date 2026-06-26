package farmix.com.backend.user;

public record CurrentUserResponse(
        Long userId,
        Long companyId,
        String email,
        String role
) {
}
