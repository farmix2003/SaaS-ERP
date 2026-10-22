package farmix.com.backend.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        BigDecimal totalRevenue,
        long totalOrders,
        long totalCustomers,
        long lowStockCount,
        OrdersSummaryResponse orderSummary,
        List<TopProductResponse> topProducts,
        List<LowStockProductResponse> lowStockProducts
) {
}
