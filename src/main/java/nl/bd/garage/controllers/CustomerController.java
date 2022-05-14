package nl.bd.garage.controllers;

import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.models.requests.CustomerNameRequest;
import nl.bd.garage.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Handles http requests on endpoints concerning customers.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    // Returns all customers.
    @GetMapping()
    List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    /*
     * Returns a customer by customer id.
     *
     * Provide customer id in the url.
     */
    @GetMapping("/{customerId}")
    Customer getCustomerById(@PathVariable long customerId) {
        return customerService.getCustomerById(customerId);
    }

    /*
     * Returns a list of customers by name. Can search by last name.
     *
     * Provide name:String in the body.
     */
    @GetMapping("/name")
    List<Customer> getCustomersByName(@RequestBody CustomerNameRequest name) {
        return customerService.getCustomersByName(name.getName());
    }

    /*
     * Creates a new customer.
     *
     * Provide name:String, telephoneNumber:String, licensePlate:String in the body.
     */
    @Secured(Role.Code.ASSISTANT)
    @PostMapping()
    Customer createCustomer(@RequestBody Customer newCustomer) {
        return customerService.createCustomer(newCustomer);
    }

    /*
     * Updates the details of an existing customer.
     *
     * Provide customer id in the url. Provide name:String, telephoneNumber:String, licensePlate:String in the body.
     */
    @Secured(Role.Code.ASSISTANT)
    @PutMapping("/{customerId}")
    Customer updateCustomer(@RequestBody Customer newCustomer, @PathVariable long customerId) {
        return customerService.updateCustomer(newCustomer, customerId);
    }

    /*
     * Deletes a customer from the database.
     *
     * Provide customer id in the url.
     */
    @Secured(Role.Code.ADMIN)
    @DeleteMapping("/{customerId}")
    void deleteCustomer(@PathVariable long customerId) {
        customerService.deleteCustomer(customerId);
    }
}
