package nl.bd.garage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.bd.garage.models.entities.CostItem;
import nl.bd.garage.models.enums.CostType;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.models.requests.AddStockRequest;
import nl.bd.garage.models.requests.CostItemRegistrationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockUser(username = "admin", password = "admin", authorities = {Role.Code.ADMIN, Role.Code.BACKOFFICE})
public class CostItemIntegrationTests {
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void clearDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "costitems");
    }

    @Test
    public void createAndGetCostItemTest() throws Exception {
        //Arrange
        CostItemRegistrationRequest costItem = initCostItems().get(0);
        String jsonBodyCostItem = objectMapper.writeValueAsString(costItem);

        //Act
        this.mockMvc.perform(post("/api/costitems").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCostItem))
                .andDo(print())
                .andExpect(status().isOk());

        //Assert
        this.mockMvc.perform(get("/api/costitems"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].name").value(costItem.getName()))
                .andExpect(jsonPath("$.[0].cost").value(costItem.getCost()))
                .andExpect(jsonPath("$.[0].costType").value(costItem.getCostType().toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void addStockAndAddStockExceptionTest() throws Exception {
        //Arrange
        CostItemRegistrationRequest costItem1 = initCostItems().get(0);
        CostItemRegistrationRequest costItem2 = initCostItems().get(1);
        AddStockRequest addStockRequest = new AddStockRequest(7);

        String jsonBodyCostItem1 = objectMapper.writeValueAsString(costItem1);
        String jsonBodyCostItem2 = objectMapper.writeValueAsString(costItem2);
        String jsonBodyAddStock = objectMapper.writeValueAsString(addStockRequest);

        //Act
        this.mockMvc.perform(post("/api/costitems").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCostItem1))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/api/costitems").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCostItem2))
                .andDo(print())
                .andExpect(status().isOk());

        String jsonResult = this.mockMvc.perform(get("/api/costitems"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CostItem> costItemListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<CostItem>>() {});
        CostItem first = costItemListResponse.get(0);
        CostItem second = costItemListResponse.get(1);

        this.mockMvc.perform(put("/api/costitems/addstock/" + first.getCostItemId())
                .contentType(APPLICATION_JSON_UTF8).content(jsonBodyAddStock));

        this.mockMvc.perform(put("/api/costitems/addstock/" + first.getCostItemId())
                .contentType(APPLICATION_JSON_UTF8).content(jsonBodyAddStock));

        //Assert
        this.mockMvc.perform(get("/api/costitems/"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].stock").value(addStockRequest.getAmount() * 2))
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/api/costitems/addstock/" + second.getCostItemId())
                .contentType(APPLICATION_JSON_UTF8).content(jsonBodyAddStock))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateCostItemAndGetByIdTest() throws Exception {
        //Arrange
        List<CostItemRegistrationRequest> costItems = initCostItems();
        CostItemRegistrationRequest costItem1 = costItems.get(0);
        CostItemRegistrationRequest costItem2 = costItems.get(3);

        String jsonBodyCostItem1 = objectMapper.writeValueAsString(costItem1);
        String jsonBodyCostItem2 = objectMapper.writeValueAsString(costItem2);

        //Act
        this.mockMvc.perform(post("/api/costitems").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCostItem1))
                .andDo(print())
                .andExpect(status().isOk());

        String jsonResult = this.mockMvc.perform(get("/api/costitems"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CostItem> costItemListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<CostItem>>() {});
        CostItem first = costItemListResponse.get(0);

        this.mockMvc.perform(put("/api/costitems/" + first.getCostItemId())
                .contentType(APPLICATION_JSON_UTF8).content(jsonBodyCostItem2));

        //Assert
        this.mockMvc.perform(get("/api/costitems"))
                .andDo(print())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].costItemId").value(first.getCostItemId()))
                .andExpect(jsonPath("$.[0].costType").value(costItem2.getCostType().toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteAndNotFoundTest() throws Exception {
        //Arrange
        CostItemRegistrationRequest costItem1 = initCostItems().get(0);
        String jsonBodyCostItem1 = objectMapper.writeValueAsString(costItem1);

        //Act
        this.mockMvc.perform(post("/api/costitems").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCostItem1))
                .andDo(print())
                .andExpect(status().isOk());

        String jsonResult = this.mockMvc.perform(get("/api/costitems"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CostItem> costItemListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<CostItem>>() {});
        CostItem first = costItemListResponse.get(0);

        this.mockMvc.perform(delete("/api/costitems/" + first.getCostItemId()));

        //Assert
        this.mockMvc.perform(get("/api/costitems/" + first.getCostItemId()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private List<CostItemRegistrationRequest> initCostItems() {
        CostItemRegistrationRequest costItem1 = new CostItemRegistrationRequest("Engine", 65.5, CostType.PART);
        CostItemRegistrationRequest costItem2 = new CostItemRegistrationRequest("Replace Engine", 100, CostType.ACTION);
        CostItemRegistrationRequest costItem3 = new CostItemRegistrationRequest("Tire", 30, CostType.PART);
        CostItemRegistrationRequest costItem4 = new CostItemRegistrationRequest();
        costItem4.setCostType(CostType.ACTION);

        List<CostItemRegistrationRequest> costItems = new ArrayList<CostItemRegistrationRequest>();
        costItems.add(costItem1);
        costItems.add(costItem2);
        costItems.add(costItem3);
        costItems.add(costItem4);

        return costItems;
    }
}
