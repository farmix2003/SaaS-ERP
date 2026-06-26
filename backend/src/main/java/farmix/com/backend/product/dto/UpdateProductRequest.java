package farmix.com.backend.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 150, message = "Product name must not exceed 150 characters")
        String name,

        @NotBlank(message = "SKU is required")
        @Size(max = 80, message = "SKU must not exceed 80 characters")
        String sku,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Sale price is required")
        @DecimalMin(value = "0.00", message = "Sale price cannot be negative")
        @Digits(integer = 13, fraction = 2, message = "Sale price format is invalid")
        BigDecimal salePrice,

        @DecimalMin(value = "0.00", message = "Cost price cannot be negative")
        @Digits(integer = 13, fraction = 2, message = "Cost price format is invalid")
        BigDecimal costPrice
) {
}
