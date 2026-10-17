package farmix.com.backend.order;

import farmix.com.backend.common.exception.BadRequestException;
import farmix.com.backend.common.exception.NotFoundException;
import farmix.com.backend.company.entity.Company;
import farmix.com.backend.company.repository.CompanyRepository;
import farmix.com.backend.customer.entity.Customer;
import farmix.com.backend.customer.repository.CustomerRepository;
import farmix.com.backend.inventory.entity.StockMovement;
import farmix.com.backend.inventory.entity.StockMovementType;
import farmix.com.backend.inventory.repository.StockMovementRepository;
import farmix.com.backend.order.dto.CreateOrderItemRequest;
import farmix.com.backend.order.dto.CreateOrderRequest;
import farmix.com.backend.order.dto.OrderResponse;
import farmix.com.backend.order.dto.UpdateOrderStatusRequest;
import farmix.com.backend.order.entity.Order;
import farmix.com.backend.order.entity.OrderItem;
import farmix.com.backend.order.entity.OrderStatus;
import farmix.com.backend.order.mapper.OrderMapper;
import farmix.com.backend.order.repository.OrderRepository;
import farmix.com.backend.product.entity.Product;
import farmix.com.backend.product.repository.ProductRepository;
import farmix.com.backend.security.CurrentUser;
import farmix.com.backend.user.entity.User;
import farmix.com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final CurrentUser currentUser;


    @Transactional
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public OrderResponse create(CreateOrderRequest req) {
        Long companyId = currentUser.getCompanyId();
        Long userId = currentUser.getUserId();

        Company company = companyRepository.getReferenceById(companyId);
        User user = userRepository.getReferenceById(userId);

        Customer customer = customerRepository.findByIdAndCompany_Id(req.customerId(), companyId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        Order order = Order.builder()
                .company(company)
                .customer(customer)
                .createdBy(user)
                .status(OrderStatus.PENDING)
                .notes(req.notes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemReq : req.items()) {
            Product product = productRepository.findByIdAndCompanyIdForUpdate(itemReq.productId(), companyId)
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            if (product.getStockQuantity() < itemReq.quantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName()
                        + ". Available: " + product.getStockQuantity() + ", Requested: " + itemReq.quantity());
            }

            BigDecimal unitPrice = product.getSalePrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));
            totalAmount = totalAmount.add(subtotal);

            int prevQuantity = product.getStockQuantity();
            int newQuantity = prevQuantity - itemReq.quantity();
            product.setStockQuantity(newQuantity);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
            order.addItem(orderItem);

            stockMovementRepository.save(StockMovement.builder()
                    .company(company)
                    .product(product)
                    .createdBy(user)
                    .type(StockMovementType.OUT)
                    .quantity(itemReq.quantity())
                    .previousQuantity(prevQuantity)
                    .newQuantity(newQuantity)
                    .reason("Order created")
                    .build());


        }
        order.setTotalAmount(totalAmount);
        return orderMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public OrderResponse getOrder(Long orderId) {
        Long companyId = currentUser.getCompanyId();
        Order order = orderRepository.findByIdAndCompany_Id(orderId, companyId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return orderMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public Page<OrderResponse> list(OrderStatus status, Long customerId, Pageable pageable){
        Long companyId = currentUser.getCompanyId();
        Pageable safePageable = sanitizePageable(pageable);
        return orderRepository.search(companyId, status, customerId, safePageable)
                .map(orderMapper::toOrderResponse);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest req){
        Long companyId = currentUser.getCompanyId();
        Long userId = currentUser.getUserId();
        User updater = userRepository.getReferenceById(userId);
        Company company = companyRepository.getReferenceById(companyId);

        Order order = orderRepository.findByIdAndCompany_Id(id, companyId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED){
            throw new BadRequestException("Cannot change status of a cancelled order");
        }

        if(req.status() == OrderStatus.CANCELLED) {
            for (OrderItem orderItem : order.getItems()) {
                Product product = productRepository.findByIdAndCompanyIdForUpdate(orderItem.getProduct().getId(), companyId)
                        .orElseThrow(() -> new NotFoundException("Product not found"));
                int prevQty = product.getStockQuantity();
                int newQty = prevQty + orderItem.getQuantity();
                product.setStockQuantity(newQty);
                stockMovementRepository.save(StockMovement.builder()
                        .company(company)
                        .product(product)
                        .createdBy(updater)
                        .type(StockMovementType.IN)
                        .quantity(orderItem.getQuantity())
                        .previousQuantity(prevQty)
                        .newQuantity(newQty)
                        .reason("Order #" + order.getId() + " cancelled")
                        .build());
            }
        }
        order.setStatus(req.status());
        order.setUpdatedBy(updater);
        return orderMapper.toOrderResponse(order);
    }

    private Pageable sanitizePageable(Pageable pageable) {
        int page = Math.max(0, pageable.getPageNumber());
        int size = Math.min(Math.max(1, pageable.getPageSize()), 100);
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

}
