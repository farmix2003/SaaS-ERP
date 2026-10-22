package farmix.com.backend.analytics.controller;

import farmix.com.backend.analytics.dto.DashboardSummaryResponse;
import farmix.com.backend.analytics.dto.LowStockProductResponse;
import farmix.com.backend.analytics.dto.OrdersSummaryResponse;
import farmix.com.backend.analytics.dto.TopProductResponse;
import farmix.com.backend.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @ResponseStatus(HttpStatus.OK)
    public DashboardSummaryResponse getDashboardSummary(){
        return analyticsService.getDashboardSummary();
    }

    @GetMapping("/orders-summary")
    public OrdersSummaryResponse getOrdersSummary() {
    return analyticsService.getOrdersSummary();
}
   @GetMapping("/top-products")
public List<TopProductResponse> getTopProducts(
        @RequestParam(required = false, defaultValue = "5") Integer limit
) {
    return analyticsService.getTopSellings(limit);
}
@GetMapping("/low-stock-products")
public List<LowStockProductResponse> getLowStockProducts(
        @RequestParam(required = false, defaultValue = "10") Integer threshold
) {
    return analyticsService.getLowStockProducts(threshold);
}

}
