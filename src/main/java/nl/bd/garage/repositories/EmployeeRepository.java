package nl.bd.garage.repositories;

import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Returns a list of customers that end in the given name (so you can also search just by last name.)
    @Query(value = "SELECT * FROM employees WHERE name LIKE %:name", nativeQuery = true)
    List<Employee> findCustomersByName(@Param("name")String name);

    Employee findByUsername(String username);
}
