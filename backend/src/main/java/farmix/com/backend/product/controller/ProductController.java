package farmix.com.backend.product.controller;

import farmix.com.backend.common.dto.PageResponse;
import farmix.com.backend.product.dto.CreateProductRequest;
import farmix.com.backend.product.dto.ProductResponse;
import farmix.com.backend.product.dto.UpdateProductRequest;
import farmix.com.backend.product.entity.ProductStatus;
import farmix.com.backend.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @Valid @RequestBody CreateProductRequest request
    ) {
        return productService.create(request);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(
            @PathVariable Long id
    ) {
        return productService.getById(id);
    }

    @GetMapping
    public PageResponse<ProductResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ProductStatus status,
            Pageable pageable
    ) {
        return PageResponse.from(
                productService.search(q, status, pageable)
        );
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productService.update(id, request);
    }

    @PatchMapping("/{id}/archive")
    public ProductResponse archive(
            @PathVariable Long id
    ) {
        return productService.archive(id);
    }
}