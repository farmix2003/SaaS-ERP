package farmix.com.backend.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long userId,
        Long companyId,
        String email,
        String role
) {
}
