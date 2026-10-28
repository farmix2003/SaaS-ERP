package farmix.com.backend.order;

import farmix.com.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("Order Workflow: Create order, deduct stock, and verify movement")
    void createOrder_ShouldDeductStockAndRecordMovement() throws Exception {
        String token = registerAndLogin();
        Long customerId = createCustomer(token, "Customer Order Test");
        Long productId = createProduct(token, "Wireless Mouse", "WM-001", 100, 25000); // 100 in stock

        // 1. Place order for 10 units
        String orderRes = mockMvc.perform(post("/api/v1/orders/create")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "customerId", customerId,
                                "items", List.of(Map.of("productId", productId, "quantity", 10)),
                                "notes", "Urgent delivery"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(250000)) // 10 * 25000
                .andReturn().getResponse().getContentAsString();

        Long orderId = jsonMapper.readTree(orderRes).get("id").asLong();

        // 2. Verify product stock was automatically reduced from 100 to 90
        mockMvc.perform(get("/api/v1/inventory/products/{id}/stock", productId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(90));

        // 3. Verify stock movement was recorded
        mockMvc.perform(get("/api/v1/inventory/movements")
                        .header("Authorization", bearer(token))
                        .param("productId", productId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("OUT"))
                .andExpect(jsonPath("$.content[0].quantity").value(10));
    }

    @Test
    @DisplayName("Order Validation: Reject order if quantity exceeds stock")
    void createOrder_ShouldRejectWhenInsufficientStock() throws Exception {
        String token = registerAndLogin();
        Long customerId = createCustomer(token, "Customer Insufficient");
        Long productId = createProduct(token, "Mechanical Keyboard", "KB-001", 5, 50000); // Only 5 in stock

        // Attempt to order 10 units
        mockMvc.perform(post("/api/v1/orders/create")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "customerId", customerId,
                                "items", List.of(Map.of("productId", productId, "quantity", 10))
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Insufficient stock")));

        // Stock remains unchanged at 5
        mockMvc.perform(get("/api/v1/inventory/products/{id}/stock", productId)
                        .header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.stockQuantity").value(5));
    }

    @Test
    @DisplayName("Order Cancellation: Stock should be restored when order is CANCELLED")
    void cancelOrder_ShouldRestoreStock() throws Exception {
        String token = registerAndLogin();
        Long customerId = createCustomer(token, "Customer Cancel Test");
        Long productId = createProduct(token, "Monitor 27 inch", "MN-27", 10, 200000); // 10 in stock

        // Place order for 4 units (leaves 6 in stock)
        String orderRes = mockMvc.perform(post("/api/v1/orders/create")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "customerId", customerId,
                                "items", List.of(Map.of("productId", productId, "quantity", 4))
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long orderId = jsonMapper.readTree(orderRes).get("id").asLong();

        // Stock is now 6
        mockMvc.perform(get("/api/v1/inventory/products/{id}/stock", productId)
                        .header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.stockQuantity").value(6));

        // Cancel the order
        mockMvc.perform(patch("/api/v1/orders/{id}/status", orderId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of("status", "CANCELLED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Stock restored back to 10
        mockMvc.perform(get("/api/v1/inventory/products/{id}/stock", productId)
                        .header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    // Helpers
    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String registerAndLogin() throws Exception {
        String email = "admin-" + UUID.randomUUID() + "@example.com";
        String password = "secret123";

        mockMvc.perform(post("/api/v1/auth/register-company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "companyName", "Company " + UUID.randomUUID(),
                                "industry", "Retail",
                                "phone", "+998901234567",
                                "companyEmail", "co-" + UUID.randomUUID() + "@example.com",
                                "firstName", "Admin",
                                "lastName", "User",
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isCreated());

        String res = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return jsonMapper.readTree(res).get("accessToken").asString();
    }

    private Long createCustomer(String token, String name) throws Exception {
        String res = mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "fullName", name,
                                "email", "c-" + UUID.randomUUID() + "@example.com"
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return jsonMapper.readTree(res).get("id").asLong();
    }

    private Long createProduct(String token, String name, String sku, int initialStock, double salePrice) throws Exception {
        String res = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "name", name,
                                "sku", sku,
                                "initialStockQuantity", initialStock,
                                "salePrice", salePrice
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return jsonMapper.readTree(res).get("id").asLong();
    }
}