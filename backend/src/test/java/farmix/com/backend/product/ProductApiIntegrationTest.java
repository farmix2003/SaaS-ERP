package farmix.com.backend.product;

import farmix.com.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void createProduct_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Notebook A5",
                          "sku": "NB-A5-001",
                          "description": "A5 notebook",
                          "salePrice": 25000,
                          "costPrice": 18000,
                          "initialStockQuantity": 100
                        }
                        """
                        )
        )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void companyAdmin_shouldCreateProduct() throws Exception {
        String token = registerAndLoginCompanyAdmin();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Notebook A5",
                                  "sku": "nb-a5-001",
                                  "description": "A5 notebook",
                                  "salePrice": 25000,
                                  "costPrice": 18000,
                                  "initialStockQuantity": 100
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Notebook A5"))
                .andExpect(jsonPath("$.sku").value("NB-A5-001"))
                .andExpect(jsonPath("$.salePrice").value(25000))
                .andExpect(jsonPath("$.costPrice").value(18000))
                .andExpect(jsonPath("$.stockQuantity").value(100))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createProduct_shouldRejectDuplicateSkuInsideSameCompany() throws Exception {
        String token = registerAndLoginCompanyAdmin();

        createProduct(token, "Notebook A5", "NB-A5-001");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Notebook A5 Duplicate",
                                  "sku": "nb-a5-001",
                                  "description": "Duplicate SKU",
                                  "salePrice": 26000,
                                  "costPrice": 19000,
                                  "initialStockQuantity": 50
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Product SKU already exists in this company in this company"));
    }

    @Test
    void searchProducts_shouldReturnStablePageResponse() throws Exception {
        String token = registerAndLoginCompanyAdmin();

        createProduct(token, "Notebook A5", "NB-A5-001");
        createProduct(token, "Notebook A4", "NB-A4-001");

        mockMvc.perform(get("/api/v1/products")
                        .param("q", "notebook")
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.empty").value(false));
    }

    @Test
    void userFromAnotherCompany_shouldNotAccessProduct() throws Exception {
        String companyAToken = registerAndLoginCompanyAdmin();
        String companyBToken = registerAndLoginCompanyAdmin();

        Long productId = createProduct(companyAToken, "Notebook A5", "NB-A5-001");

        mockMvc.perform(get("/api/v1/products/{id}", productId)
                        .header("Authorization", bearer(companyBToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void archiveProduct_shouldMarkProductAsArchived() throws Exception {
        String token = registerAndLoginCompanyAdmin();

        Long productId = createProduct(token, "Notebook A5", "NB-A5-001");

        mockMvc.perform(patch("/api/v1/products/{id}/archive", productId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    private String registerAndLoginCompanyAdmin() throws Exception {
        String email = "admin-" + UUID.randomUUID() + "@example.com";
        String password = "secret123";

        mockMvc.perform(post("/api/v1/auth/register-company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Company %s",
                                  "industry": "Retail",
                                  "phone": "+998901234567",
                                  "companyEmail": "company-%s@example.com",
                                  "firstName": "Muhammad",
                                  "lastName": "Ibrohimov",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(UUID.randomUUID(), UUID.randomUUID(), email, password)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = jsonMapper.readTree(loginResponse);

        return json.get("accessToken").asText();
    }

    private Long createProduct(String token, String name, String sku) throws Exception {
        String response = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "sku": "%s",
                                  "description": "Test product",
                                  "salePrice": 25000,
                                  "costPrice": 18000,
                                  "initialStockQuantity": 100
                                }
                                """.formatted(name, sku)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = jsonMapper.readTree(response);

        assertThat(json.get("id").asLong()).isPositive();

        return json.get("id").asLong();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
