package farmix.com.backend.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @NotBlank(message = "Customer full name is required")
        @Size(max = 150, message = "Full name must not exceed 150 characters")
        String fullName,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        String notes
) {
}
