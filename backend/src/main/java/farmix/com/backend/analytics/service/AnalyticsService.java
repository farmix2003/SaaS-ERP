package farmix.com.backend.analytics.service;

import farmix.com.backend.analytics.dto.*;
import farmix.com.backend.customer.repository.CustomerRepository;
import farmix.com.backend.order.entity.OrderStatus;
import farmix.com.backend.order.repository.OrderItemRepository;
import farmix.com.backend.order.repository.OrderRepository;
import farmix.com.backend.product.dto.ProductResponse;
import farmix.com.backend.product.entity.Product;
import farmix.com.backend.product.entity.ProductStatus;
import farmix.com.backend.product.repository.ProductRepository;
import farmix.com.backend.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CurrentUser currentUser;

    private static final int DEFAULT_LOW_STOCk_THRESHOLD = 10;
    private static final int DEFAULT_TOP_PRODUCTS_LIMIT = 5;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public RevenueSummaryResponse getRevenueSummary(){
        Long companyId = currentUser.getCompanyId();

        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(companyId);
        BigDecimal paidRevenue = orderRepository.calculatePaidRevenue(companyId);

        long totalOrders = orderRepository.countByCompany_Id(companyId);

        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if(totalOrders > 0 && totalRevenue.compareTo(BigDecimal.ZERO)> 0){
            averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_DOWN);
        }
        return new RevenueSummaryResponse(totalRevenue, paidRevenue,totalOrders,averageOrderValue);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public OrdersSummaryResponse getOrdersSummary(){
        Long companyId = currentUser.getCompanyId();

        long total = orderRepository.countByCompany_Id(companyId);
        long pending = orderRepository.countByCompany_IdAndStatus(companyId, OrderStatus.PENDING);
        long paid = orderRepository.countByCompany_IdAndStatus(companyId, OrderStatus.PAID);
        long shipped = orderRepository.countByCompany_IdAndStatus(companyId, OrderStatus.SHIPPED);
        long cancelled = orderRepository.countByCompany_IdAndStatus(companyId, OrderStatus.CANCELLED);
        return new OrdersSummaryResponse(total, pending, paid, shipped, cancelled);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public List<TopProductResponse> getTopSellings(Integer limit){
       Long companyId = currentUser.getCompanyId();

       int max = (limit == null || limit <=0) ? DEFAULT_TOP_PRODUCTS_LIMIT : Math.min(limit, 50);
       return orderItemRepository.findTopProducts(companyId, PageRequest.of(0, max));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public List<LowStockProductResponse> getLowStockProducts(Integer threshold){
        Long companyId = currentUser.getCompanyId();
        int limitThreshold = (threshold == null || threshold < 0) ? DEFAULT_LOW_STOCk_THRESHOLD : threshold;

        return productRepository.findLowStockProducts(companyId, limitThreshold).stream()
                .map(p -> new LowStockProductResponse(
                        p.getId(),
                        p.getName(),
                        p.getSku(),
                        p.getStockQuantity(),
                        limitThreshold
                ))
                .toList();
    }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
  public DashboardSummaryResponse getDashboardSummary(){
        Long companyId = currentUser.getCompanyId();

        RevenueSummaryResponse revenueSummaryResponse = getRevenueSummary();
        OrdersSummaryResponse ordersSummaryResponse = getOrdersSummary();
        List<TopProductResponse> topProductResponses = getTopSellings(5);
        List<LowStockProductResponse> lowStockProductResponses = getLowStockProducts(DEFAULT_LOW_STOCk_THRESHOLD);

        long totalCustomers = customerRepository.countByCompany_Id(companyId);
        long lowStockCount = productRepository.countByCompany_IdAndStatusAndStockQuantityLessThanEqual(companyId, ProductStatus.ACTIVE, DEFAULT_LOW_STOCk_THRESHOLD);

        return new DashboardSummaryResponse(
                revenueSummaryResponse.totalRevenue(),
                ordersSummaryResponse.totalOrders(),
                totalCustomers,
                lowStockCount,
                ordersSummaryResponse,
                topProductResponses,
                lowStockProductResponses
        );
  }

}
