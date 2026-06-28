package farmix.com.backend.auth;

import farmix.com.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void registerCompany_shouldCreateCompanyAndAdminUser() throws Exception {
        String email = "admin-" + UUID.randomUUID() + "@example.com";

        String response = mockMvc.perform(post("/api/v1/auth/register-company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "ABC Market",
                                  "industry": "Retail",
                                  "phone": "+998901234567",
                                  "companyEmail": "company@example.com",
                                  "firstName": "Muhammad",
                                  "lastName": "Ibrohimov",
                                  "email": "%s",
                                  "password": "secret123"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("COMPANY_ADMIN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = jsonMapper.readTree(response);

        assertThat(node.get("companyId").asLong()).isPositive();
        assertThat(node.get("userId").asLong()).isPositive();
    }

    @Test
    void login_shouldReturnJwtTokenAndUserContext() throws Exception{
        String email = "admin-" + UUID.randomUUID() + "@example.com";

        registerCompany(email, "secret123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "%s",
                            "password": "secret123"
                            }
                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.companyId").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("COMPANY_ADMIN"));

    }

    @Test
    void registerCompany_shouldRejectDuplicateEmail() throws Exception {
        String email = "admin-" + UUID.randomUUID() + "@example.com";

        registerCompany(email, "secret123");

        mockMvc.perform(post("/api/v1/auth/register-company")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                                "companyName": "Another Company",
                "industry": "Retail",
                "phone": "+998901234567",
                "companyEmail": "another@example.com",
                "firstName": "Ali",
                "lastName": "Valiyev",
                "email": "%s",
                "password": "secret123"
                                }
        """.formatted(email)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
}

private void registerCompany(String email, String password) throws Exception {
mockMvc.perform(post("/api/v1/auth/register-company")
.contentType(MediaType.APPLICATION_JSON)
.content("""
                                {
                                  "companyName": "ABC Market",
                                  "industry": "Retail",
                                  "phone": "+998901234567",
                                  "companyEmail": "company@example.com",
                                  "firstName": "Muhammad",
                                  "lastName": "Ibrohimov",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isCreated());
    }

}