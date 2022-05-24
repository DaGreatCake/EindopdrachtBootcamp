package nl.bd.garage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.models.requests.NameRequest;
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
@WithMockUser(username = "admin", password = "admin", authorities = {Role.Code.ADMIN, Role.Code.ASSISTANT})
public class CustomerIntegrationTests {
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
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "customers");
    }

    @Test
    public void createAndGetCustomerTest() throws Exception {
        //Arrange
        Customer customer = initCustomers().get(0);
        String jsonBodyCustomer = objectMapper.writeValueAsString(customer);

        //Act
        this.mockMvc.perform(post("/api/customers").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCustomer))
                .andDo(print())
                .andExpect(status().isOk());

        //Assert
        this.mockMvc.perform(get("/api/customers"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].name").value(customer.getName()))
                .andExpect(jsonPath("$.[0].telephoneNumber").value(customer.getTelephoneNumber()))
                .andExpect(jsonPath("$.[0].licensePlate").value(customer.getLicensePlate()))
                .andExpect(status().isOk());
    }

    @Test
    public void getCustomersByNameTest() throws Exception {
        //Arrange
        List<Customer> customers = initCustomers();
        NameRequest nameRequest = new NameRequest("Veldhuizen");

        String jsonBodyCustomer1 = objectMapper.writeValueAsString(customers.get(0));
        String jsonBodyCustomer2 = objectMapper.writeValueAsString(customers.get(1));
        String jsonBodyCustomer3 = objectMapper.writeValueAsString(customers.get(2));
        String jsonBodyName = objectMapper.writeValueAsString(nameRequest);

        //Act
        this.mockMvc.perform(post("/api/customers").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCustomer1))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/api/customers").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCustomer2))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/api/customers").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCustomer3))
                .andDo(print())
                .andExpect(status().isOk());

        //Assert
        this.mockMvc.perform(get("/api/customers/name")
                        .contentType(APPLICATION_JSON_UTF8).content(jsonBodyName))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].telephoneNumber").value(customers.get(0).getTelephoneNumber()))
                .andExpect(jsonPath("$.[1].telephoneNumber").value(customers.get(1).getTelephoneNumber()))
                .andExpect(status().isOk());
    }

    @Test
    public void updateCustomerAndGetByIdTest() throws Exception {
        //Arrange
        List<Customer> customers = initCustomers();
        Customer customer1 = customers.get(0);
        Customer customer2 = customers.get(3);

        String jsonBodyCustomer1 = objectMapper.writeValueAsString(customer1);
        String jsonBodyCustomer2 = objectMapper.writeValueAsString(customer2);

        //Act
        this.mockMvc.perform(post("/api/customers").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCustomer1))
                .andDo(print())
                .andExpect(status().isOk());

        String jsonResult = this.mockMvc.perform(get("/api/customers"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Customer> customerListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<Customer>>() {});
        Customer first = customerListResponse.get(0);

        this.mockMvc.perform(put("/api/customers/" + first.getCustomerId())
                .contentType(APPLICATION_JSON_UTF8).content(jsonBodyCustomer2));

        //Assert
        this.mockMvc.perform(get("/api/customers"))
                .andDo(print())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].customerId").value(first.getCustomerId()))
                .andExpect(jsonPath("$.[0].telephoneNumber").value(customer2.getTelephoneNumber()))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteAndNotFoundTest() throws Exception {
        //Arrange
        Customer customer1 = initCustomers().get(0);
        String jsonBodyCustomer1 = objectMapper.writeValueAsString(customer1);

        //Act
        this.mockMvc.perform(post("/api/customers").contentType(APPLICATION_JSON_UTF8).content(jsonBodyCustomer1))
                .andDo(print())
                .andExpect(status().isOk());

        String jsonResult = this.mockMvc.perform(get("/api/customers"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Customer> customerListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<Customer>>() {});
        Customer first = customerListResponse.get(0);

        this.mockMvc.perform(delete("/api/customers/" + first.getCustomerId()));

        //Assert
        this.mockMvc.perform(get("/api/customers/" + first.getCustomerId()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private List<Customer> initCustomers() {
        Customer customer1 = new Customer("Benjamin Veldhuizen", "0612345678", "AB-12-CD");
        Customer customer2 = new Customer("Jaap Veldhuizen", "0600000000", "AA-11-AA");
        Customer customer3 = new Customer("Jip Sterk", "0611111111", "BB-22-BB");
        Customer customer4 = new Customer();
        customer4.setTelephoneNumber("0699999999");

        List<Customer> customers = new ArrayList<Customer>();
        customers.add(customer1);
        customers.add(customer2);
        customers.add(customer3);
        customers.add(customer4);

        return customers;
    }
}
