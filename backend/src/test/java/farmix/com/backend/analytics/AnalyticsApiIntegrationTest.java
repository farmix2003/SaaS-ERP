package farmix.com.backend.analytics;

import farmix.com.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnalyticsApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("GET /api/v1/analytics/dashboard - Returns aggregated business summary")
    void getDashboardSummary_ShouldReturnTotals() throws Exception {
        String token = registerAndLogin();
        Long customerId = createCustomer(token);
        Long product1 = createProduct(token, "USB-C Cable", "USB-C-01", 100, 10000);
        Long product2 = createProduct(token, "Power Bank", "PB-20K", 5, 50000); // Low stock (5 <= 10)
        // Create an order for 2 USB cables
        mockMvc.perform(post("/api/v1/orders/create")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "customerId", customerId,
                                "items", List.of(Map.of("productId", product1, "quantity", 2))
                        ))))
                .andExpect(status().isCreated());
        // Check Dashboard
        mockMvc.perform(get("/api/v1/analytics/dashboard")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(20000))
                .andExpect(jsonPath("$.totalOrders").value(1))
                .andExpect(jsonPath("$.totalCustomers").value(1))
                .andExpect(jsonPath("$.lowStockCount").value(1)) // Power Bank
                .andExpect(jsonPath("$.topProducts[0].productName").value("USB-C Cable"));
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
        return jsonMapper.readTree(res).get("accessToken").asText();
    }
    private Long createCustomer(String token) throws Exception {
        String res = mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "fullName", "Test Customer",
                                "email", "tc-" + UUID.randomUUID() + "@example.com"
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(res).get("id").asLong();
    }
    private Long createProduct(String token, String name, String sku, int stock, double price) throws Exception {
        String res = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "name", name,
                                "sku", sku,
                                "initialStockQuantity", stock,
                                "salePrice", price
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(res).get("id").asLong();
    }

}
