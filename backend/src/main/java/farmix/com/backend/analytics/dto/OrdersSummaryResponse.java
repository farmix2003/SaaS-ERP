package farmix.com.backend.analytics.dto;

public record OrdersSummaryResponse(
        long totalOrders,
        long pendingOrders,
        long paidOrders,
        long shippedOrders,
        long cancelledOrders
) {
}
