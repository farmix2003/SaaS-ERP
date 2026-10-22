package farmix.com.backend.analytics.dto;

public record LowStockProductResponse(
        Long productId,
        String productName,
        String sku,
        int currentStock,
        int lowStockThreshold
) {
}
