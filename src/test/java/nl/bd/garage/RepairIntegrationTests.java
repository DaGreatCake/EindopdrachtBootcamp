package nl.bd.garage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.bd.garage.models.entities.CostItem;
import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.entities.Repair;
import nl.bd.garage.models.enums.CostType;
import nl.bd.garage.models.enums.RepairStatus;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.models.requests.*;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
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
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
@WithMockUser(username = "admin", password = "admin", authorities = 
        {Role.Code.ADMIN, Role.Code.BACKOFFICE, Role.Code.CASHIER, Role.Code.ASSISTANT, Role.Code.MECHANIC})
public class RepairIntegrationTests {
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void clearDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "repairs", "customers", "costitems");
    }

    @Test
    @Order(1)
    public void createAndGetRepairTest() throws Exception {
        //Arrange
        Customer customer = new Customer("Benjamin Veldhuizen", "0612345678", "AB-12-CD");
        String jsonBodyCustomer = objectMapper.writeValueAsString(customer);

        this.mockMvc.perform(post("/api/customers").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCustomer))
                .andDo(print())
                .andExpect(status().isOk());

        String jsonResult = this.mockMvc.perform(get("/api/customers"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Customer> customerListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<Customer>>() {});
        Customer first = customerListResponse.get(0);

        RepairRegistrationRequest repair = new RepairRegistrationRequest(first.getCustomerId(), new Date(122, 3, 28));
        String jsonBodyRepair = objectMapper.writeValueAsString(repair);

        //Act
        this.mockMvc.perform(post("/api/repairs").contentType(APPLICATION_JSON_UTF8).content(jsonBodyRepair))
                .andDo(print())
                .andExpect(status().isOk());

        //Assert
        this.mockMvc.perform(get("/api/repairs"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].customer.customerId").value(first.getCustomerId()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    public void incorrectSyntaxAndPreviousStepUncompletedTest() throws Exception {
        //Arrange
        RepairSetFoundProblemsRequest repairSetFoundProblemsRequest = new RepairSetFoundProblemsRequest();
        String jsonBodyRepairSetFoundProblems = objectMapper.writeValueAsString(repairSetFoundProblemsRequest);

        //Act

        String jsonResult = this.mockMvc.perform(get("/api/repairs"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Repair> repairListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<Repair>>() {});
        Repair first = repairListResponse.get(0);

        //Assert
        this.mockMvc.perform(put("/api/repairs/cancel/" + first.getRepairId()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/api/repairs/examined/" + first.getRepairId())
                .contentType(APPLICATION_JSON_UTF8).content(jsonBodyRepairSetFoundProblems))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    public void setFoundProblemsTest() throws Exception {
        //Arrange
        RepairSetFoundProblemsRequest repairSetFoundProblemsRequest =
                new RepairSetFoundProblemsRequest("Engine and tires need replacing.");
        String jsonBodySetFoundProblems = objectMapper.writeValueAsString(repairSetFoundProblemsRequest);

        String jsonResult = this.mockMvc.perform(get("/api/repairs"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Repair> repairListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<Repair>>() {});
        Repair first = repairListResponse.get(0);

        //Act
        this.mockMvc.perform(put("/api/repairs/examined/" + first.getRepairId())
                        .contentType(APPLICATION_JSON_UTF8).content(jsonBodySetFoundProblems))
                .andDo(print())
                .andExpect(status().isOk());

        //Assert
        this.mockMvc.perform(get("/api/repairs/"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].foundProblems").value(repairSetFoundProblemsRequest.getFoundProblems()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    public void setRepairDateTest() throws Exception {
        //Arrange
        RepairSetRepairDateRequest repairSetRepairDateRequest =
                new RepairSetRepairDateRequest(new java.sql.Date(122, 3,29));
        String jsonBodySetRepairDate = objectMapper.writeValueAsString(repairSetRepairDateRequest);

        String jsonResult = this.mockMvc.perform(get("/api/repairs"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Repair> repairListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<Repair>>() {});
        Repair first = repairListResponse.get(0);

        //Act
        this.mockMvc.perform(put("/api/repairs/setrepairdate/" + first.getRepairId())
                        .contentType(APPLICATION_JSON_UTF8).content(jsonBodySetRepairDate))
                .andDo(print())
                .andExpect(status().isOk());

        //Assert
        this.mockMvc.perform(get("/api/repairs/"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].repairDate").value(repairSetRepairDateRequest.getRepairDate().toString()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    public void setPartsAndPartsOutOfStockExceptionTest() throws Exception {
        //Arrange
        CostItemRegistrationRequest costItemRegistrationRequest1 =
                new CostItemRegistrationRequest("Engine", 100, CostType.PART);
        CostItemRegistrationRequest costItemRegistrationRequest2 =
                new CostItemRegistrationRequest("Replace Engine", 65.5, CostType.ACTION);
        AddStockRequest addStockRequest = new AddStockRequest(3);

        String jsonBodyCreateCostItem1 = objectMapper.writeValueAsString(costItemRegistrationRequest1);
        String jsonBodyCreateCostItem2 = objectMapper.writeValueAsString(costItemRegistrationRequest2);
        String jsonBodyAddStock = objectMapper.writeValueAsString(addStockRequest);

        this.mockMvc.perform(post("/api/costitems").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCreateCostItem1))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/api/costitems").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCreateCostItem2))
                .andDo(print())
                .andExpect(status().isOk());

        String jsonResultCostItems = this.mockMvc.perform(get("/api/costitems"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CostItem> costItemListResponse = objectMapper.readValue(jsonResultCostItems, new TypeReference<List<CostItem>>() {});
        CostItem firstCostItem = costItemListResponse.get(0);
        CostItem secondCostItem = costItemListResponse.get(1);

        List<Long> ids = new ArrayList<>();
        ids.add(firstCostItem.getCostItemId());
        ids.add(secondCostItem.getCostItemId());

        RepairSetPartsRequest repairSetPartsRequest =
                new RepairSetPartsRequest(ids, 0);
        String jsonBodySetParts = objectMapper.writeValueAsString(repairSetPartsRequest);

        String jsonResultRepairs = this.mockMvc.perform(get("/api/repairs"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Repair> repairListResponse = objectMapper.readValue(jsonResultRepairs, new TypeReference<List<Repair>>() {});
        Repair firstRepair = repairListResponse.get(0);

        //Act
        this.mockMvc.perform(put("/api/repairs/setparts/" + firstRepair.getRepairId())
                        .contentType(APPLICATION_JSON_UTF8).content(jsonBodySetParts))
                .andDo(print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/api/costitems/addstock/" + firstCostItem.getCostItemId())
                .contentType(APPLICATION_JSON_UTF8).content(jsonBodyAddStock));

        this.mockMvc.perform(put("/api/repairs/setparts/" + firstRepair.getRepairId())
                        .contentType(APPLICATION_JSON_UTF8).content(jsonBodySetParts))
                .andDo(print())
                .andExpect(status().isOk());

        //Assert
        this.mockMvc.perform(get("/api/costitems/" + firstCostItem.getCostItemId()))
                .andDo(print())
                .andExpect(jsonPath("$.stock").value(addStockRequest.getAmount() - 1))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/api/repairs/"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].partsUsed[0]").value(ids.get(0)))
                .andExpect(jsonPath("$.[0].partsUsed[1]").value(ids.get(1)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    public void cancelRepairAndDisagreedExceptionTest() throws Exception {
        //Arrange
        Customer customer = new Customer("Jip Sterk", "0600000000", "AA-11-BB");
        String jsonBodyCustomer = objectMapper.writeValueAsString(customer);

        this.mockMvc.perform(post("/api/customers").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCustomer))
                .andDo(print())
                .andExpect(status().isOk());

        String jsonResultCustomers = this.mockMvc.perform(get("/api/customers"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Customer> customerListResponse = objectMapper.readValue(jsonResultCustomers, new TypeReference<List<Customer>>() {});
        Customer secondCustomer = customerListResponse.get(1);

        RepairRegistrationRequest repairRegistrationRequest =
                new RepairRegistrationRequest(secondCustomer.getCustomerId(), new java.sql.Date(122, 3,28));
        String jsonBodyRepairRegistration = objectMapper.writeValueAsString(repairRegistrationRequest);

        RepairSetFoundProblemsRequest repairSetFoundProblemsRequest =
                new RepairSetFoundProblemsRequest("Entire car needs replacing.");
        String jsonBodyFoundProblems = objectMapper.writeValueAsString(repairSetFoundProblemsRequest);

        //Act
        this.mockMvc.perform(post("/api/repairs").contentType(APPLICATION_JSON_UTF8).content(jsonBodyRepairRegistration))
                .andDo(print())
                .andExpect(status().isOk());

        String jsonResultRepairs = this.mockMvc.perform(get("/api/repairs"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Repair> repairListResponse = objectMapper.readValue(jsonResultRepairs, new TypeReference<List<Repair>>() {});
        Repair second = repairListResponse.get(1);

        this.mockMvc.perform(put("/api/repairs/examined/" + second.getRepairId())
                        .contentType(APPLICATION_JSON_UTF8).content(jsonBodyFoundProblems))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/api/repairs/cancel/" + second.getRepairId()))
                .andDo(print())
                .andExpect(status().isOk());

        //Assert
        this.mockMvc.perform(get("/api/repairs/"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[1].customerAgreed").value(false))
                .andExpect(jsonPath("$.[1].completed").value(RepairStatus.CANCELED.toString()))
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/api/repairs/setrepaircompleted/" + second.getRepairId()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    public void setCompleteAndGetToCallTest() throws Exception {
        //Arrange
        String jsonResult = this.mockMvc.perform(get("/api/repairs"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Repair> repairListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<Repair>>() {});
        Repair first = repairListResponse.get(0);

        //Act
        this.mockMvc.perform(get("/api/repairs/completed"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        this.mockMvc.perform(put("/api/repairs/setrepaircompleted/" + first.getRepairId()))
                .andDo(print())
                .andExpect(status().isOk());

        //Assert
        this.mockMvc.perform(get("/api/repairs/"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].completed").value(RepairStatus.COMPLETED.toString()))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/api/repairs/completed"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @Order(8)
    public void setCalledAndSetPaymentCompleteTest() throws Exception {
        //Arrange
        String jsonResult = this.mockMvc.perform(get("/api/repairs"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Repair> repairListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<Repair>>() {});
        Repair first = repairListResponse.get(0);

        //Act
        this.mockMvc.perform(put("/api/repairs/setcalled/" + first.getRepairId()))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/api/repairs/setpaymentcompleted/" + first.getRepairId()))
                .andDo(print())
                .andExpect(status().isOk());

        //Assert
        this.mockMvc.perform(get("/api/repairs/"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].called").value(true))
                .andExpect(jsonPath("$.[0].paid").value(true))
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    public void deleteAndNotFoundTest() throws Exception {
        //Arrange
        String jsonResult = this.mockMvc.perform(get("/api/repairs"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Repair> repairListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<Repair>>() {});
        Repair first = repairListResponse.get(0);

        //Act
        this.mockMvc.perform(delete("/api/repairs/" + first.getRepairId()));

        //Assert
        this.mockMvc.perform(get("/api/repairs/" + first.getRepairId()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        clearDatabase();
    }
}
