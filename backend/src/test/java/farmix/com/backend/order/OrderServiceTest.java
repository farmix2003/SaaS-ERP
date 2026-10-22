package farmix.com.backend.order;

import farmix.com.backend.audit.service.AuditLogService;
import farmix.com.backend.company.entity.Company;
import farmix.com.backend.company.repository.CompanyRepository;
import farmix.com.backend.customer.entity.Customer;
import farmix.com.backend.customer.repository.CustomerRepository;
import farmix.com.backend.inventory.entity.StockMovement;
import farmix.com.backend.inventory.entity.StockMovementType;
import farmix.com.backend.inventory.repository.StockMovementRepository;
import farmix.com.backend.order.dto.CreateOrderItemRequest;
import farmix.com.backend.order.dto.CreateOrderRequest;
import farmix.com.backend.order.dto.OrderItemResponse;
import farmix.com.backend.order.dto.OrderResponse;
import farmix.com.backend.order.entity.Order;
import farmix.com.backend.order.entity.OrderStatus;
import farmix.com.backend.order.mapper.OrderMapper;
import farmix.com.backend.order.repository.OrderRepository;
import farmix.com.backend.order.service.OrderService;
import farmix.com.backend.product.entity.Product;
import farmix.com.backend.product.repository.ProductRepository;
import farmix.com.backend.security.CurrentUser;
import farmix.com.backend.user.entity.User;
import farmix.com.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CurrentUser currentUser;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private OrderService orderService;

    private Company mockCompany;
    private User mockUser;
    private Customer mockCustomer;
    private CreateOrderRequest validRequest;

    @BeforeEach
    void setUp() {
        // Shared Mock Data
        mockCompany = new Company();
        mockUser = new User();
        mockCustomer = new Customer();

        // Setup Current User Context Mocking
        when(currentUser.getCompanyId()).thenReturn(10L);
        when(currentUser.getUserId()).thenReturn(20L);
    }


    @Test
    @DisplayName("POST /api/orders - Create Order: Should create a new order successfully")
    void createOrder_Success() {
        // Arrange
        CreateOrderItemRequest itemReq = new CreateOrderItemRequest(100L, 2); // productId, quantity
        validRequest = new CreateOrderRequest(5L,List.of(itemReq), "Test notes"); // customerId, notes, items

        Product mockProduct = new Product();
        mockProduct.setId(100L);
        mockProduct.setName("Laptop");
        mockProduct.setSku("LP100");
        mockProduct.setStockQuantity(10);
        mockProduct.setSalePrice(new BigDecimal("1000.00"));

        when(companyRepository.getReferenceById(10L)).thenReturn(mockCompany);
        when(userRepository.getReferenceById(20L)).thenReturn(mockUser);
        when(customerRepository.findByIdAndCompany_Id(5L, 10L)).thenReturn(Optional.of(mockCustomer));
        when(productRepository.findByIdAndCompanyIdForUpdate(100L, 10L)).thenReturn(Optional.of(mockProduct));

        OrderResponse expectedResponse = new OrderResponse(1L, 10L, 5L, "Jane", OrderStatus.PENDING, new BigDecimal(100),"Test note", List.of(new OrderItemResponse(1L, mockProduct.getId(), mockProduct.getName(), mockProduct.getSku(), 10, mockProduct.getSalePrice(), new BigDecimal("2000.00"))), LocalDateTime.now(), null); // Stub mapped response
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(expectedResponse);

        // Act
        OrderResponse actualResponse = orderService.create(validRequest);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(8, mockProduct.getStockQuantity()); // Stock deducted: 10 - 2 = 8

        // Verify Stock Movement was saved correctly using Argument Captor
        ArgumentCaptor<StockMovement> stockCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository, times(1)).save(stockCaptor.capture());

        StockMovement movement = stockCaptor.getValue();
        assertEquals(StockMovementType.OUT, movement.getType());
        assertEquals(2, movement.getQuantity());
        assertEquals(10, movement.getPreviousQuantity());
        assertEquals(8, movement.getNewQuantity());
    }

}
