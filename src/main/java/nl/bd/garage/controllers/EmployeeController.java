package nl.bd.garage.controllers;

import nl.bd.garage.models.entities.Employee;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Handles http requests on endpoints concerning employees.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    // Returns all employees.
    @Secured(Role.Code.ADMIN)
    @GetMapping()
    List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    /*
     * Returns an employee by employee id.
     *
     * Provide employee id in the url.
     */
    @Secured(Role.Code.ADMIN)
    @GetMapping("/{employeeId}")
    Employee getEmployeeById(@PathVariable long employeeId) {
        return employeeService.getEmployeeById(employeeId);
    }

    /*
     * Creates a new employee in the database.
     *
     * Provide username:String, password:String, name:String, role:Role in the body.
     */
    @Secured(Role.Code.ADMIN)
    @PostMapping()
    Employee createEmployee(@RequestBody Employee newEmployee) {
        return employeeService.createEmployee(newEmployee);
    }

    /*
     * Updates details of an existing employee.
     *
     * Provide employee id in the url. Provide username:String, password:String, name:String, role:Role in the body.
     */
    @Secured(Role.Code.ADMIN)
    @PutMapping("/{employeeId}")
    Employee updateEmployee(@RequestBody Employee newEmployee, @PathVariable long employeeId) {
        return employeeService.updateEmployee(newEmployee, employeeId);
    }

    /*
     * Deletes an employee from the database.
     *
     * Provide employee id in the url.
     */
    @Secured(Role.Code.ADMIN)
    @DeleteMapping("/{employeeId}")
    void deleteEmployee(@PathVariable long employeeId) {
        employeeService.deleteEmployee(employeeId);
    }
}
