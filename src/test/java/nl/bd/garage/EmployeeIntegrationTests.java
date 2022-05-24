package nl.bd.garage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.bd.garage.models.entities.Employee;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockUser(username = "admin", password = "admin", authorities = {Role.Code.ADMIN})
public class EmployeeIntegrationTests {
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
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "employees");
    }

    @Test
    public void createAndUsernameExistsEmployeeTest() throws Exception {
        //Arrange
        Employee employee = initEmployees().get(0);
        String jsonBodyEmployee = objectMapper.writeValueAsString(employee);

        //Act
        this.mockMvc.perform(post("/api/employees").contentType(APPLICATION_JSON_UTF8).content(jsonBodyEmployee))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/api/employees").contentType(APPLICATION_JSON_UTF8).content(jsonBodyEmployee))
                .andDo(print())
                .andExpect(status().isBadRequest());

        //Assert
        String jsonResult = this.mockMvc.perform(get("/api/employees"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Employee> employeeListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<Employee>>() {});
        Employee first = employeeListResponse.get(0);

        assertThat(first.getUsername().equals(employee.getUsername()));
        assertThat(first.getPassword().equals(passwordEncoder.encode(employee.getPassword())));
        assertThat(first.getName().equals(employee.getName()));
        assertThat(first.getRole().equals(employee.getRole()));
    }

    @Test
    public void getEmployeesByNameTest() throws Exception {
        //Arrange
        List<Employee> employees = initEmployees();
        NameRequest nameRequest = new NameRequest("Veldhuizen");

        String jsonBodyEmployee1 = objectMapper.writeValueAsString(employees.get(0));
        String jsonBodyEmployee2 = objectMapper.writeValueAsString(employees.get(1));
        String jsonBodyEmployee3 = objectMapper.writeValueAsString(employees.get(2));
        String jsonBodyName = objectMapper.writeValueAsString(nameRequest);

        //Act
        this.mockMvc.perform(post("/api/employees").contentType(APPLICATION_JSON_UTF8).content(jsonBodyEmployee1))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/api/employees").contentType(APPLICATION_JSON_UTF8).content(jsonBodyEmployee2))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/api/employees").contentType(APPLICATION_JSON_UTF8).content(jsonBodyEmployee3))
                .andDo(print())
                .andExpect(status().isOk());

        //Assert
        this.mockMvc.perform(get("/api/employees/name")
                        .contentType(APPLICATION_JSON_UTF8).content(jsonBodyName))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].username").value(employees.get(0).getUsername()))
                .andExpect(jsonPath("$.[1].username").value(employees.get(1).getUsername()))
                .andExpect(status().isOk());
    }

    @Test
    public void updateEmployeeAndGetByIdTest() throws Exception {
        //Arrange
        List<Employee> employees = initEmployees();
        Employee employee1 = employees.get(0);
        Employee employee2 = employees.get(3);

        String jsonBodyEmployee1 = objectMapper.writeValueAsString(employee1);
        String jsonBodyEmployee2 = objectMapper.writeValueAsString(employee2);

        //Act
        this.mockMvc.perform(post("/api/employees").contentType(APPLICATION_JSON_UTF8).content(jsonBodyEmployee1))
                .andDo(print())
                .andExpect(status().isOk());

        String jsonResult = this.mockMvc.perform(get("/api/employees"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Employee> employeeListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<Employee>>() {});
        Employee first = employeeListResponse.get(0);

        this.mockMvc.perform(put("/api/employees/" + first.getEmployeeId())
                .contentType(APPLICATION_JSON_UTF8).content(jsonBodyEmployee2));

        //Assert
        this.mockMvc.perform(get("/api/employees"))
                .andDo(print())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].employeeId").value(first.getEmployeeId()))
                .andExpect(jsonPath("$.[0].username").value(employee2.getUsername()))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteAndNotFoundTest() throws Exception {
        //Arrange
        Employee employee1 = initEmployees().get(0);
        String jsonBodyEmployee1 = objectMapper.writeValueAsString(employee1);

        //Act
        this.mockMvc.perform(post("/api/employees").contentType(APPLICATION_JSON_UTF8).content(jsonBodyEmployee1))
                .andDo(print())
                .andExpect(status().isOk());

        String jsonResult = this.mockMvc.perform(get("/api/employees"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Employee> employeeListResponse = objectMapper.readValue(jsonResult, new TypeReference<List<Employee>>() {});
        Employee first = employeeListResponse.get(0);

        this.mockMvc.perform(delete("/api/employees/" + first.getEmployeeId()));

        //Assert
        this.mockMvc.perform(get("/api/employees/" + first.getEmployeeId()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private List<Employee> initEmployees() {
        Employee employee1 = new Employee("ben01", "pass1", "Benjamin Veldhuizen", Role.BACKOFFICE);
        Employee employee2 = new Employee("ben02", "pass2", "Jaap Veldhuizen", Role.MECHANIC);
        Employee employee3 = new Employee("ben03", "pass3", "Jip Sterk", Role.CASHIER);
        Employee employee4 = new Employee();
        employee4.setUsername("benupdated");

        List<Employee> employees = new ArrayList<Employee>();
        employees.add(employee1);
        employees.add(employee2);
        employees.add(employee3);
        employees.add(employee4);

        return employees;
    }
}
