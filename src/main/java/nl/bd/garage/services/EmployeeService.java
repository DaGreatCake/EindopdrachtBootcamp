package nl.bd.garage.services;

import nl.bd.garage.models.entities.Employee;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.models.exceptions.ModifyAdminException;
import nl.bd.garage.models.exceptions.EmployeeNotFoundException;
import nl.bd.garage.models.exceptions.UsernameAlreadyExistsException;
import nl.bd.garage.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * This service will handle all the functionality that is being requested through EmployeeController and interact with
 * the database accordingly.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Returns all employees.
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /*
     * Returns an employee by id.
     *
     * Will throw exception if employee id doesnt exist.
     */
    public Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId).orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }

    // Returns a list of customers by name.
    public List<Employee> getEmployeesByName(String name) {
        return employeeRepository.findCustomersByName(name);
    }

    /*
     * Creates new employee in the database.
     *
     * Will throw exception if the new role is ADMIN.
     */
    public Employee createEmployee(Employee newEmployee) {
        if (employeeRepository.findByUsername(newEmployee.getUsername()) != null) {
            throw new UsernameAlreadyExistsException(newEmployee.getUsername());
        } else if (newEmployee.getRole() == Role.ADMIN) {
            throw new ModifyAdminException("create");
        } else {
            newEmployee.setPassword(passwordEncoder.encode(newEmployee.getPassword()));
            return employeeRepository.save(newEmployee);
        }
    }

    /*
     * Modifies an employee in the database.
     *
     * Will throw exception if the new role is ADMIN or the employee id doesnt exist.
     */
    public Employee updateEmployee(Employee newEmployee, Long employeeId) {
        if (employeeRepository.findByUsername(newEmployee.getUsername()) != null) {
            throw new UsernameAlreadyExistsException(newEmployee.getUsername());
        } else {
            return employeeRepository.findById(employeeId)
                    .map(employee -> {
                        if (employee.getRole() != Role.ADMIN && newEmployee.getRole() == Role.ADMIN) {
                            throw new ModifyAdminException("create");
                        } else if (employee.getRole() == Role.ADMIN && newEmployee.getRole() != Role.ADMIN) {
                            throw new ModifyAdminException("delete");
                        }

                        if (newEmployee.getUsername() != null) {
                            employee.setUsername(newEmployee.getUsername());
                        }
                        if (newEmployee.getPassword() != null) {
                            employee.setPassword(passwordEncoder.encode(newEmployee.getPassword()));
                        }
                        if (newEmployee.getName() != null) {
                            employee.setName(newEmployee.getName());
                        }
                        if (newEmployee.getRole() != null) {
                            employee.setRole(newEmployee.getRole());
                        }

                        return employeeRepository.save(employee);
                    })
                    .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        }
    }

    /*
     * Deletes an employee from the database.
     *
     * Will throw exception if the role of that employee is ADMIN or if the employee id doesnt exist.
     */
    public void deleteEmployee(Long employeeId) {
        if (employeeRepository.findById(employeeId).isPresent()) {
            if (employeeRepository.findById(employeeId).get().getRole() == Role.ADMIN) {
                throw new ModifyAdminException("delete");
            } else {
                employeeRepository.deleteById(employeeId);
            }
        } else {
            throw new EmployeeNotFoundException(employeeId);
        }
    }
}
