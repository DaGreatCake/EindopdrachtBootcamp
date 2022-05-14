package nl.bd.garage.repositories;

import nl.bd.garage.models.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Returns a list of customers that end in the given name (so you can also search just by last name.)
    @Query(value = "SELECT * FROM customers WHERE name LIKE %:name", nativeQuery = true)
    List<Customer> findCustomersByName(@Param("name")String name);

    // Returns a list of customers from a list of customer ids.
    @Query(value = "SELECT * FROM customers WHERE customer_id IN :ids", nativeQuery = true)
    List<Customer> findCustomersByIdList(@Param("ids")List<Long> ids);
}

