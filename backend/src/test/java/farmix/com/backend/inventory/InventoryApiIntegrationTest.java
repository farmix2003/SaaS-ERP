package farmix.com.backend.inventory;

import farmix.com.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InventoryApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void stockIn_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/stock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(Map.of("productId", 1L,"quantity", 50,
                        "reason", "Initial stock")))
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void stockIn_shouldIncreaseProductStockAndCreateMovement() throws Exception {
        String token = registerAndLoginCompanyAdmin();
        Long productId = createProduct(token,"Test Product", "TP001");
        
        mockMvc.perform(post("/api/v1/inventory/stock-in")
                        .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(Map.of(
                        "productId", productId,
                        "quantity", 50,
                        "reason", "Initial stock"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.type").value("IN"))
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.previousQuantity").value(0))
                .andExpect(jsonPath("$.newQuantity").value(50))
                .andExpect(jsonPath("$.reason").value("Initial stock"));


        mockMvc.perform(get("/api/v1/inventory/products/{productId}/stock", productId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.stockQuantity").value(50));
    }

    @Test
    void stockOut_shouldDecreaseProductStockAndCreateMovement() throws Exception {
        String token = registerAndLoginCompanyAdmin();
        Long productId = createProduct(token, "Test product", "TP001");

        stockIn(token, productId, 100);

        mockMvc.perform(post("/api/v1/inventory/stock-out")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(Map.of(
                        "productId", productId,
                        "quantity", 50,
                        "reason", "Customer order"
                )))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.type").value("OUT"))
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.previousQuantity").value(100))
                .andExpect(jsonPath("$.newQuantity").value(50));

        mockMvc.perform(get("/api/v1/inventory/products/{productId}/stock", productId)
                .header("Authorization", bearer(token))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(50));
    }

    @Test
    void stockOut_shouldRejectInsufficientStock() throws Exception {
        String token = registerAndLoginCompanyAdmin();
        Long productId = createProduct(token, "Test product", "TP001");

        stockIn(token, productId, 30);

        mockMvc.perform(post("/api/v1/inventory/stock-out")
               .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(Map.of(
                        "productId", productId,
                        "quantity", 50,
                        "reason", "Customer order"
                )))
        ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("quantity less than or equal to request quantity"));

        mockMvc.perform(get("/api/v1/inventory/products/{productId}/stock", productId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(30));
    }

    @Test
    void adjust_shouldSetExactStockQuantityAndCreateMovement() throws Exception {
        String token = registerAndLoginCompanyAdmin();
        Long productId = createProduct(token, "Test product", "TP001");

        stockIn(token, productId, 100);

        mockMvc.perform(post("/api/v1/inventory/adjust")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(Map.of(
                        "productId", productId,
                        "newQuantity", 50,
                        "reason", "Initial stock"
        )))).andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.type").value("ADJUSTMENT"))
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.previousQuantity").value(100))
                .andExpect(jsonPath("$.newQuantity").value(50));

        mockMvc.perform(get("/api/v1/inventory/products/{productId}/stock", productId)
                .header("Authorization", bearer(token))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(50));
    }

    @Test
    void adjust_shouldRejectSameQuantity() throws Exception {
        String token = registerAndLoginCompanyAdmin();
        Long productId = createProduct(token, "Test product", "TP001");

        stockIn(token, productId, 100);

        mockMvc.perform(post("/api/v1/inventory/adjust")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "productId", productId,
                                "newQuantity", 100,
                                "reason", "Initial stock"
                        )))
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("New quantity must be different from current quantity"));

    }

    @Test
    void searchMovements_shouldReturnStablePageResponse() throws Exception {
        String token = registerAndLoginCompanyAdmin();
        Long productId = createProduct(token, "Test product", "TP001");

        stockIn(token, productId, 100);
        stockOut(token, productId, 40);

        mockMvc.perform(get("/api/v1/inventory/movements")
                        .param("productId", productId.toString())
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.empty").value(false));

    }

    @Test
    void userFromAnotherCompany_shouldNotChangeOtherCompanyProductStock() throws Exception {
        String companyAToken = registerAndLoginCompanyAdmin();
        String companyBToken = registerAndLoginCompanyAdmin();

        Long companyAProductId = createProduct(companyAToken, "Notebook A5", "NB-A5-001");


        mockMvc.perform(post("/api/v1/inventory/stock-in")
                        .header("Authorization", bearer(companyBToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "productId", companyAProductId,
                                "quantity", 50,
                                "reason", "Attempted stock in by another company"
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));

        mockMvc.perform(get("/api/v1/inventory/products/{productId}/stock", companyAProductId)
                        .header("Authorization", bearer(companyAToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(0));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
    private Long createProduct(String token, String testProduct, String tp001) throws Exception {
        String response = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "name", testProduct,
                                "sku", tp001,
                                "description", "Test product",
                                "salePrice", 25000,
                                "costPrice", 18000,
                                "initialStockQuantity", 0
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = jsonMapper.readTree(response);

        Long productId = json.get("id").asLong();

        assertThat(productId).isPositive();

        return productId;
    }
    private String registerAndLoginCompanyAdmin() throws Exception {
        String email = "admin-" + UUID.randomUUID() + "@example.com";
        String password = "secret123";

        mockMvc.perform(post("/api/v1/auth/register-company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "companyName", "Company " + UUID.randomUUID(),
                                "industry", "Retail",
                                "phone", "+998901234567",
                                "companyEmail", "company-" + UUID.randomUUID() + "@example.com",
                                "firstName", "Muhammad",
                                "lastName", "Ibrohimov",
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
    private void stockIn(String token, Long productId, int quantity) throws Exception {
        mockMvc.perform(post("/api/v1/inventory/stock-in")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "productId", productId,
                                "quantity", quantity,
                                "reason", "Test stock in"
                        ))))
                .andExpect(status().isOk());
    }
    private void stockOut(String token, Long productId, int quantity) throws Exception {
        mockMvc.perform(post("/api/v1/inventory/stock-out")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(Map.of(
                                "productId", productId,
                                "quantity", quantity,
                                "reason", "Test stock out"
                        ))))
                .andExpect(status().isOk());
    }
}
