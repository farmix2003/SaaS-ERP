package farmix.com.backend.inventory.mapper;

import farmix.com.backend.inventory.dto.StockMovementResponse;
import farmix.com.backend.inventory.entity.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovementResponse toResponse(StockMovement movement) {
        return new  StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getName(),
                movement.getProduct().getSku(),
                movement.getType().name(),
                movement.getQuantity(),
                movement.getPreviousQuantity(),
                movement.getNewQuantity(),
                movement.getReason(),
                movement.getCreatedAt()
        );
    }

}
