package farmix.com.backend.inventory.service;

import farmix.com.backend.observability.BusinessMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import farmix.com.backend.common.exception.BadRequestException;
import farmix.com.backend.common.exception.NotFoundException;
import farmix.com.backend.company.entity.Company;
import farmix.com.backend.company.repository.CompanyRepository;
import farmix.com.backend.inventory.dto.ProductStockResponse;
import farmix.com.backend.inventory.dto.StockAdjustmentRequest;
import farmix.com.backend.inventory.dto.StockInRequest;
import farmix.com.backend.inventory.dto.StockMovementResponse;
import farmix.com.backend.inventory.dto.StockOutRequest;
import farmix.com.backend.inventory.entity.StockMovement;
import farmix.com.backend.inventory.entity.StockMovementType;
import farmix.com.backend.inventory.mapper.StockMovementMapper;
import farmix.com.backend.inventory.repository.StockMovementRepository;
import farmix.com.backend.product.entity.Product;
import farmix.com.backend.product.repository.ProductRepository;
import farmix.com.backend.security.CurrentUser;
import farmix.com.backend.user.entity.User;
import farmix.com.backend.user.repository.UserRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Validated
public class InventoryService {

    private final ProductRepository productRepository;
    private final StockMovementRepository  stockMovementRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final StockMovementMapper mapper;
    private final CurrentUser currentUser;
    private final BusinessMetrics businessMetrics;

    @Transactional
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN','MANAGER')")
    public StockMovementResponse stockIn(StockInRequest request){
        Product product = getLockedProduct(request.productId());

        int previousQuantity = product.getStockQuantity();
        int newQuantity = previousQuantity + request.quantity();

        product.setStockQuantity(newQuantity);

        StockMovement stockMovement = saveMovement(
                product,
                StockMovementType.IN,
                request.quantity(),
                previousQuantity,
                newQuantity,
                normalizeReason(request.reason())
        );

        businessMetrics.stockIn();

        return mapper.toResponse(stockMovement);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public StockMovementResponse stockOut(StockOutRequest request){
        Product product = getLockedProduct(request.productId());

        int previousQuantity = product.getStockQuantity();
        if (previousQuantity < request.quantity()){
            businessMetrics.insufficientStock();
            throw new BadRequestException("quantity less than or equal to request quantity");
        }

        int newQuantity = previousQuantity - request.quantity();
        product.setStockQuantity(newQuantity);

        StockMovement stockMovement = saveMovement(
                product,
                StockMovementType.OUT,
                request.quantity(),
                previousQuantity,
                newQuantity,
                normalizeReason(request.reason())
        );

        businessMetrics.stockOut();

        return mapper.toResponse(stockMovement);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public StockMovementResponse adjust(StockAdjustmentRequest request) {
        Product product = getLockedProduct(request.productId());

        int previousQuantity = product.getStockQuantity();
        int newQuantity = request.newQuantity();

        int movementQuantity = Math.abs(request.newQuantity() - previousQuantity);
        if (movementQuantity == 0) {
            throw new BadRequestException("New quantity must be different from current quantity");
        }
        product.setStockQuantity(newQuantity);

        StockMovement stockMovement = saveMovement(
                product,
                StockMovementType.ADJUSTMENT,
                request.newQuantity(),
                previousQuantity,
                newQuantity,
                normalizeReason(request.reason())
        );

        businessMetrics.stockAdjusted();

        return mapper.toResponse(stockMovement);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ProductStockResponse getProductStock(Long productId){
        Long companyId = currentUser.getCompanyId();

        Product product = productRepository.findByIdAndCompany_Id(productId, companyId).orElseThrow(() -> new NotFoundException("Product not found"));

        return new ProductStockResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getStockQuantity()
        );
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public Page<StockMovementResponse> searchMovements(
            Long productId,
            StockMovementType type,
            Pageable pageable
    ) {
        Long companyId = currentUser.getCompanyId();

        Pageable safePageable = sanitizePageable(pageable);

        return stockMovementRepository
                .search(companyId, productId, type, safePageable)
                .map(mapper::toResponse);
    }

    private Pageable sanitizePageable(Pageable pageable) {
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = Math.min(Math.max(pageable.getPageSize(), 1), 100);

        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private StockMovement saveMovement(Product product, StockMovementType type, @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be greater than zero") Integer quantity, int previousQuantity, int newQuantity, String reason) {

        Long companyId = currentUser.getCompanyId();
        Long userId = currentUser.getUserId();

        Company company = companyRepository.getReferenceById(companyId);
        User user = userRepository.getReferenceById(userId);

        StockMovement movement = StockMovement.builder()
                .company(company)
                .product(product)
                .createdBy(user)
                .type(type)
                .quantity(quantity)
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .reason(reason)
                .build();

        return stockMovementRepository.save(movement);

    }


    private String normalizeReason(@Size(max = 500, message = "Reason must not exeed 500 characters") String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }

        return reason.trim();
    }

    private Product getLockedProduct(@NotNull(message = "Product id is required") Long productId) {
        Long companyId = currentUser.getCompanyId();

        return productRepository.findByIdAndCompany_Id(companyId, productId).orElseThrow(() -> new NotFoundException("Product not found"));
    }

}
