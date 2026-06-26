package farmix.com.backend.product.service;

import farmix.com.backend.common.exception.ConflictException;
import farmix.com.backend.common.exception.NotFoundException;
import farmix.com.backend.company.entity.Company;
import farmix.com.backend.product.dto.CreateProductRequest;
import farmix.com.backend.product.dto.ProductResponse;
import farmix.com.backend.product.dto.UpdateProductRequest;
import farmix.com.backend.product.entity.Product;
import farmix.com.backend.product.entity.ProductStatus;
import farmix.com.backend.product.mapper.ProductMapper;
import farmix.com.backend.product.repository.ProductRepository;
import farmix.com.backend.repository.CompanyRepository;
import farmix.com.backend.repository.UserRepository;
import farmix.com.backend.security.CurrentUser;
import farmix.com.backend.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final CurrentUser currentUser;

    @Transactional
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ProductResponse create(CreateProductRequest createProductRequest) {
        Long companyId = currentUser.getCompanyId();
        Long userId = currentUser.getUserId();

        String normalizeSKU = normalizeSku(createProductRequest.sku());

        if(productRepository.existsByCompany_IdAndSkuIgnoreCase(companyId, normalizeSKU)){
            throw new ConflictException("Product already exists");
        }
        Company company = companyRepository.getReferenceById(companyId);
        User creator = userRepository.getReferenceById(userId);

        Product product = Product.builder()
                .company(company)
                .createdBy(creator)
                .name(createProductRequest.name())
                .sku(normalizeSku(createProductRequest.sku()))
                .description(normalizeNullableText(createProductRequest.description()))
                .salePrice(createProductRequest.salePrice())
                .costPrice(createProductRequest.costPrice())
                .stockQuantity(createProductRequest.initialStockQuantity() == null ? 0: createProductRequest.initialStockQuantity())
                .status(ProductStatus.ACTIVE)
                .build();

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ProductResponse getById(Long id){
        Long companyId = currentUser.getCompanyId();

        Product product = productRepository.findByIdAndCompany_Id(id, companyId).orElseThrow(()->new NotFoundException("Product not found"));
        return productMapper.toResponse(product);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public Page<ProductResponse> search(String q, ProductStatus status, Pageable pageable){
        Long companyId = currentUser.getCompanyId();

        String normalizedQuery = normalizeSearchQuery(q);

        Pageable safePageable = sanitizePageable(pageable);

        return productRepository.search(companyId, normalizedQuery, status, safePageable)
                .map(productMapper::toResponse);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Long companyId = currentUser.getCompanyId();
        Long userId = currentUser.getUserId();

        Product product = productRepository.findByIdAndCompany_Id(id, companyId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        String normalizedSku = normalizeSku(request.sku());

        if (productRepository.existsByCompany_IdAndSkuIgnoreCaseAndIdNot(
                companyId,
                normalizedSku,
                id
        )) {
            throw new ConflictException("Product SKU already exists in this company");
        }

        User updater = userRepository.getReferenceById(userId);

        product.setUpdatedBy(updater);
        product.setName(request.name().trim());
        product.setSku(normalizedSku);
        product.setDescription(normalizeNullableText(request.description()));
        product.setSalePrice(request.salePrice());
        product.setCostPrice(request.costPrice());

        return productMapper.toResponse(product);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ProductResponse archive(Long id) {
        Long companyId = currentUser.getCompanyId();
        Long userId = currentUser.getUserId();

        Product product = productRepository.findByIdAndCompany_Id(id, companyId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        User updater = userRepository.getReferenceById(userId);

        product.setUpdatedBy(updater);
        product.setStatus(ProductStatus.ARCHIVED);

        return productMapper.toResponse(product);
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase();
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeSearchQuery(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }

        return q.trim();
    }

    private Pageable sanitizePageable(Pageable pageable) {
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = Math.min(Math.max(pageable.getPageSize(), 1), 100);

        Sort safeSort = Sort.by(Sort.Direction.DESC, "createdAt");

        return PageRequest.of(page, size, safeSort);
    }

}
