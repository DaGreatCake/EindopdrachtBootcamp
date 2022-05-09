package nl.bd.garage.controllers;

import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.entities.Employee;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.repositories.CustomerRepository;
import nl.bd.garage.services.CustomerService;
import nl.bd.garage.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Secured(Role.Code.ADMIN)
    @GetMapping()
    List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @Secured(Role.Code.ADMIN)
    @GetMapping("/{employeeId}")
    Employee getEmployeeById(@PathVariable long employeeId) {
        return employeeService.getEmployeeById(employeeId);
    }

    @Secured(Role.Code.ADMIN)
    @PostMapping()
    Employee createEmployee(@RequestBody Employee newEmployee) {
        return employeeService.createEmployee(newEmployee);
    }

    @Secured(Role.Code.ADMIN)
    @PutMapping("/{employeeId}")
    Employee updateEmployee(@RequestBody Employee newEmployee, @PathVariable long employeeId) {
        return employeeService.updateEmployee(newEmployee, employeeId);
    }

    @Secured(Role.Code.ADMIN)
    @DeleteMapping("/{employeeId}")
    void deleteEmployee(@PathVariable long employeeId) {
        employeeService.deleteEmployee(employeeId);
    }
}
