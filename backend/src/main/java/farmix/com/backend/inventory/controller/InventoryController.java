package farmix.com.backend.inventory.controller;

import farmix.com.backend.common.dto.PageResponse;
import farmix.com.backend.inventory.dto.*;
import farmix.com.backend.inventory.entity.StockMovementType;
import farmix.com.backend.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/stock-in")
    public ResponseEntity<StockMovementResponse> stockIn(@Valid @RequestBody StockInRequest req){
        return new ResponseEntity<>(inventoryService.stockIn(req), HttpStatus.OK);
    }

    @PostMapping("/stock-out")
    public ResponseEntity<StockMovementResponse> stockOut(@Valid @RequestBody StockOutRequest req){
        return new ResponseEntity<>(inventoryService.stockOut(req), HttpStatus.OK);
    }

    @PostMapping("/adjust")
    public ResponseEntity<StockMovementResponse> adjust(@Valid @RequestBody StockAdjustmentRequest req){
        return new ResponseEntity<>(inventoryService.adjust(req), HttpStatus.OK);
    }

    @GetMapping("/movements")
    public ResponseEntity<PageResponse<StockMovementResponse>> searchMovements(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) StockMovementType type,
            Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(
                inventoryService.searchMovements(productId, type, pageable)
        ));
    }

    @GetMapping("/products/{productId}/stock")
    public ResponseEntity<ProductStockResponse> getProductStock(@PathVariable("productId") Long productId){
        return new ResponseEntity<>(inventoryService.getProductStock(productId), HttpStatus.OK);
    }



}
