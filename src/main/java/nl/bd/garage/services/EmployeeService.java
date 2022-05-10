package nl.bd.garage.services;

import nl.bd.garage.models.entities.Employee;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.models.exceptions.ModifyAdminException;
import nl.bd.garage.models.exceptions.EmployeeNotFoundException;
import nl.bd.garage.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(long employeeId) {
        return employeeRepository.findById(employeeId).orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }

    public Employee createEmployee(Employee newEmployee) {
        if (newEmployee.getRole() == Role.ADMIN) {
            throw new ModifyAdminException("create");
        } else {
            newEmployee.setPassword(passwordEncoder.encode(newEmployee.getPassword()));
            return employeeRepository.save(newEmployee);
        }
    }

    public Employee updateEmployee(Employee newEmployee, Long employeeId) {
        if (newEmployee.getRole() == Role.ADMIN) {
            throw new ModifyAdminException("create");
        } else {
            return employeeRepository.findById(employeeId)
                    .map(employee -> {
                        employee.setUsername(newEmployee.getUsername());
                        employee.setPassword(passwordEncoder.encode(newEmployee.getPassword()));
                        employee.setName(newEmployee.getName());
                        employee.setRole(newEmployee.getRole());
                        return employeeRepository.save(employee);
                    })
                    .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        }
    }

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
