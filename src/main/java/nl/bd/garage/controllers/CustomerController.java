package nl.bd.garage.controllers;

import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.repositories.CustomerRepository;
import nl.bd.garage.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping()
    List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{customerId}")
    Customer getCustomerById(@PathVariable long customerId) {
        return customerService.getCustomerById(customerId);
    }

    @Secured({Role.Code.ASSISTANT, Role.Code.ADMIN})
    @PostMapping()
    Customer createCustomer(@RequestBody Customer newCustomer) {
        return customerService.createCustomer(newCustomer);
    }

    @Secured({Role.Code.ASSISTANT, Role.Code.ADMIN})
    @PutMapping("/{customerId}")
    Customer updateCustomer(@RequestBody Customer newCustomer, @PathVariable long customerId) {
        return customerService.updateCustomer(newCustomer, customerId);
    }

    @Secured(Role.Code.ADMIN)
    @DeleteMapping("/{customerId}")
    void deleteCustomer(@PathVariable long customerId) {
        customerService.deleteCustomer(customerId);
    }
}
