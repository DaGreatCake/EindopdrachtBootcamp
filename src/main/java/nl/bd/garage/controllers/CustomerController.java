package nl.bd.garage.controllers;

import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.repositories.CustomerRepository;
import nl.bd.garage.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping("/customers")
    List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/customers/{customerId}")
    Customer getCustomerById(@PathVariable long customerId) {
        return customerService.getCustomerById(customerId);
    }

    @PostMapping("/customers")
    Customer createCustomer(@RequestBody Customer newCustomer) {
        return customerService.createCustomer(newCustomer);
    }

    @PutMapping("/customers/{customerId}")
    Customer updateCustomer(@RequestBody Customer newCustomer, @PathVariable long customerId) {
        return customerService.updateCustomer(newCustomer, customerId);
    }

    @DeleteMapping("/customers/{customerId}")
    void deleteCustomer(@PathVariable long customerId) {
        customerService.deleteCustomer(customerId);
    }
}
