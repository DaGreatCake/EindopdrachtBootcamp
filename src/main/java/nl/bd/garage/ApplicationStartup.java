package nl.bd.garage;

import nl.bd.garage.models.entities.Employee;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * This class will create test users if any user roles are missing in the database.
 */
@Component
@Profile("prod")
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        List<Employee> employees = employeeRepository.findAll();
        boolean assistantExists = false, mechanicExists = false, cashierExists = false,
                backofficeExists = false, adminExists = false;

        for (Employee employee : employees) {
            switch (employee.getRole()) {
                case ASSISTANT -> assistantExists = true;
                case MECHANIC -> mechanicExists = true;
                case CASHIER -> cashierExists = true;
                case BACKOFFICE -> backofficeExists = true;
                case ADMIN -> adminExists = true;
            }
        }

        if (!assistantExists) {
            Employee employee = new Employee("assistant", passwordEncoder.encode("assistant"),
                    "Assistant Assistant", Role.ASSISTANT);
            employeeRepository.save(employee);
        }
        if (!mechanicExists) {
            Employee employee = new Employee("mechanic", passwordEncoder.encode("mechanic"),
                    "Mechanic Mechanic", Role.MECHANIC);
            employeeRepository.save(employee);
        }
        if (!cashierExists) {
            Employee employee = new Employee("cashier", passwordEncoder.encode("cashier"),
                    "Cashier Cashier", Role.CASHIER);
            employeeRepository.save(employee);
        }
        if (!backofficeExists) {
            Employee employee = new Employee("backoffice", passwordEncoder.encode("backoffice"),
                    "Backoffice Backoffice", Role.BACKOFFICE);
            employeeRepository.save(employee);
        }
        if (!adminExists) {
            Employee employee = new Employee("admin", passwordEncoder.encode("admin"),
                    "Admin Admin", Role.ADMIN);
            employeeRepository.save(employee);
        }
    }
}
