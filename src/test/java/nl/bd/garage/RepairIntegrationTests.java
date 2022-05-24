package nl.bd.garage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.entities.Repair;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.models.requests.AddStockRequest;
import nl.bd.garage.models.requests.RepairRegistrationRequest;
import nl.bd.garage.models.requests.RepairSetFoundProblemsRequest;
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
import java.sql.Date;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    public void incorrectSyntaxAndPartOutOfStockExceptionTest() throws Exception {
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
    public void setFoundProblemsTest() throws Exception {
        //Arrange
        RepairSetFoundProblemsRequest repairSetFoundProblemsRequest
                = new RepairSetFoundProblemsRequest("Engine and tires need replacing.");
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
    public void deleteAndNotFoundTest() throws Exception {
        //Arrange
        String jsonResult = this.mockMvc.perform(get("/api/repairs"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
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
