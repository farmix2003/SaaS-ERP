package farmix.com.backend.order.mapper;

import farmix.com.backend.order.dto.OrderItemResponse;
import farmix.com.backend.order.dto.OrderResponse;
import farmix.com.backend.order.entity.Order;
import farmix.com.backend.order.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order) {

        List<OrderItemResponse> orderItems = order.getItems().stream()
                .map(this::toOrderItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomer().getCompany().getId(),
                order.getCustomer().getId(),
                order.getCustomer().getFullName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getNotes(),
                orderItems,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

        private OrderItemResponse toOrderItemResponse(OrderItem orderItem){
            return new OrderItemResponse(
                    orderItem.getId(),
                    orderItem.getProduct().getId(),
                    orderItem.getProduct().getName(),
                    orderItem.getProduct().getSku(),
                    orderItem.getQuantity(),
                    orderItem.getUnitPrice(),
                    orderItem.getSubtotal()
            );
        }

}
