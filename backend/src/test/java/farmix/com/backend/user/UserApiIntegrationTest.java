package farmix.com.backend.user;


import farmix.com.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("POST api/v1/users - Should require authentication")
    void createUser_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(Map.of(
                        "firstName", "John",
                        "lastName", "Doe",
                        "email", "john.doe@example.com",
                        "password", "password123",
                        "role", "MANAGER"
                ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST api/v1/users - Should create user when authenticated as COMPANY_ADMIN")
    void createUser_shouldCreateUserWhenAuthenticatedAsCompanyAdmin() throws Exception {
        String token = registerAndLoginCompanyAdmin();

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "firstName", "John",
                                "lastName", "Doe",
                                "email", "john.doe@example.com",
                                "password", "password123",
                                "role", "MANAGER"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/v1/users - Should return paginated list of users when authenticated as COMPANY_ADMIN")
    void listUsers_shouldReturnPaginatedListWhenAuthenticatedAsCompanyAdmin() throws Exception {
        String token = registerAndLoginCompanyAdmin();

        createStaffUser(token, "Alice", "manager1"+UUID.randomUUID()+"@gmail.com", "MANAGER");
        createStaffUser(token, "Bob", "staff1"+UUID.randomUUID()+"@gmail.com", "EMPLOYEE");

        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", bearer(token))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));

    }

    @Test
    @DisplayName("Tenant Isolation - User from Company B should NOT access User from Company A")
    void tenantIsolation_ShouldPreventCrossCompanyAccess() throws Exception{
        String tokenA = registerAndLoginCompanyAdmin();
        String tokenB = registerAndLoginCompanyAdmin();

        Long companyId = createStaffUser(tokenA, "Alice", "manager1"+UUID.randomUUID()+"@gmail.com", "MANAGER");

        mockMvc.perform(get("/api/v1/users/{id}", companyId)
                        .header("Authorization", bearer(tokenB)))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("Tenant Isolation - User from Company A should access their own user")
    void tenantIsolation_ShouldAllowAccessToOwnCompanyUser() throws Exception{
        String tokenA = registerAndLoginCompanyAdmin();

        Long userId = createStaffUser(tokenA, "Alice", "manager1"+UUID.randomUUID()+"@gmail.com", "MANAGER");

        mockMvc.perform(get("/api/v1/users/{id}", userId)
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/deactivate - Should deactivate target user")
    void deactivateUser_ShouldMarkUserAsInactive() throws Exception{
        String token = registerAndLoginCompanyAdmin();

        Long companyId = createStaffUser(token, "Alice", "manager1"+UUID.randomUUID()+"@gmail.com", "MANAGER");

        mockMvc.perform(patch("/api/v1/users/{id}/deactivate", companyId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String registerAndLoginCompanyAdmin() throws Exception {
        String email = "admin" + UUID.randomUUID() + "@example.com";
        String password = "password123";

        mockMvc.perform(post("/api/v1/auth/register-company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "companyName", "Company " + UUID.randomUUID(),
                                "industry", "Retail",
                                "phone", "+998901234567",
                                "companyEmail", "company-" + UUID.randomUUID() + "@example.com",
                                "firstName", "Admin",
                                "lastName", "User",
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isCreated());
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = jsonMapper.readTree(loginResponse);
        return json.get("accessToken").asString();
    }
    private Long createStaffUser(String token, String firstName, String email, String role) throws Exception {
        String response = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "firstName", firstName,
                                "lastName", "Testing",
                                "email", email,
                                "password", "secret123",
                                "role", role
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = jsonMapper.readTree(response);
        return json.get("id").asLong();
    }
}
