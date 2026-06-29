package farmix.com.backend.inventory.dto;

public record ProductStockResponse(
        Long productId,
        String productName,
        String sku,
        Integer stockQuantity
) {
}
