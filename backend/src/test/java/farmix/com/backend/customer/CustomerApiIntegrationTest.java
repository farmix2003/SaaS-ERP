package farmix.com.backend.customer;

import farmix.com.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CustomerApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("POST /api/v1/customers - Requires authentication")
    void createCustomer_ShouldRequireAuth() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of("fullName", "John Doe"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/customers - Admin should create customer")
    void createCustomer_ShouldSucceed() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "fullName", "Alice Walker",
                                "email", "alice@example.com",
                                "phone", "+998901112233",
                                "address", "Tashkent, Uzbekistan"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fullName").value("Alice Walker"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/v1/customers - Should return paginated customers")
    void searchCustomers_ShouldReturnPage() throws Exception {
        String token = registerAndLogin();
        createCustomer(token, "Customer One", "c1@test.com");
        createCustomer(token, "Customer Two", "c2@test.com");

        mockMvc.perform(get("/api/v1/customers")
                        .header("Authorization", bearer(token))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("Tenant Isolation - Company B cannot view Company A's customer")
    void tenantIsolation_ShouldPreventCrossCompanyAccess() throws Exception {
        String tokenA = registerAndLogin();
        String tokenB = registerAndLogin();

        Long customerId = createCustomer(tokenA, "Company A Customer", "ca@test.com");

        mockMvc.perform(get("/api/v1/customers/{id}", customerId)
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/customers/{id}/archive - Should archive customer")
    void archiveCustomer_ShouldChangeStatus() throws Exception {
        String token = registerAndLogin();
        Long customerId = createCustomer(token, "Active Customer", "active@test.com");

        mockMvc.perform(patch("/api/v1/customers/{id}/archive", customerId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    // Helper methods
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

    private Long createCustomer(String token, String name, String email) throws Exception {
        String res = mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "fullName", name,
                                "email", email,
                                "phone", "+998901234567"
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return jsonMapper.readTree(res).get("id").asLong();
    }
}