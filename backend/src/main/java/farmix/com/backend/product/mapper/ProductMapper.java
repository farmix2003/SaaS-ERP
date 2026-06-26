package farmix.com.backend.product.mapper;

import farmix.com.backend.product.dto.ProductResponse;
import farmix.com.backend.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getSalePrice(),
                product.getCostPrice(),
                product.getStockQuantity(),
                product.getStatus().name(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

}
