package farmix.com.backend.order.controller;

import farmix.com.backend.order.OrderService;
import farmix.com.backend.order.dto.CreateOrderRequest;
import farmix.com.backend.order.dto.OrderResponse;
import farmix.com.backend.order.dto.UpdateOrderStatusRequest;
import farmix.com.backend.order.entity.OrderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;


    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request){
        return orderService.create(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<OrderResponse> getOrders(@RequestParam(name = "status", required = false) OrderStatus status, @RequestParam(name = "customerId", required = false) Long customerId, Pageable pageable){
        return orderService.list(status, customerId, pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOrder(@PathVariable Long id){
        return orderService.getOrder(id);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request){
        return orderService.updateStatus(id, request);
    }
}
