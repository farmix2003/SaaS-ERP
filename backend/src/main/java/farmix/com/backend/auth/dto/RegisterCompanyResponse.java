package farmix.com.backend.auth.dto;

public record RegisterCompanyResponse(
        Long companyId,
        Long userId,
        String email,
        String role,
        String message
) {
}
