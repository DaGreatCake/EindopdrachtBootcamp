package nl.bd.garage.services;

import nl.bd.garage.models.EmployeePrincipal;
import nl.bd.garage.models.entities.Employee;
import nl.bd.garage.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
 * Handles the details of an employee during login.
 */
@Service
public class EmployeeDetailsService implements UserDetailsService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByUsername(username);

        if (employee == null) {
            throw new UsernameNotFoundException(username);
        }

        return new EmployeePrincipal(employee);
    }
}
