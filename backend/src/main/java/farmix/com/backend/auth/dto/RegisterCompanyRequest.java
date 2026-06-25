package farmix.com.backend.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCompanyRequest(
        @NotBlank(message = "Company name is required")
        String companyName,

        String industry,

        String phone,

        @Email(message = "Company email must be valid")
        String companyEmail,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        String email,

        @Size(min = 6, message = "Password must be at least 6 characters")
        @NotBlank(message = "Password is required")
        String password
) {



}
