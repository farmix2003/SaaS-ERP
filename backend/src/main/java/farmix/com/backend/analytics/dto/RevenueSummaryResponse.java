package farmix.com.backend.analytics.dto;

import java.math.BigDecimal;

public record RevenueSummaryResponse(
        BigDecimal totalRevenue,
        BigDecimal paidRevenue,
        long totalOrders,
        BigDecimal averageOrderValue
) {
}
