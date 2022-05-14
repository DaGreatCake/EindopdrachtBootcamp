package nl.bd.garage.services;

import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.exceptions.CustomerNotFoundException;
import nl.bd.garage.models.requests.CustomerNameRequest;
import nl.bd.garage.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * This service will handle all the functionality that is being requested through CustomerController and interact with
 * the database accordingly.
 */
@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    // Returns all customers.
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    /*
     * Returns a customer by id.
     *
     * Will throw exception if the customer id doesnt exist.
     */
    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    // Returns a list of customers by name.
    public List<Customer> getCustomersByName(String name) {
        return customerRepository.findCustomersByName(name);
    }

    // Creates a new customer in the database.
    public Customer createCustomer(Customer newCustomer) {
        return customerRepository.save(newCustomer);
    }

    /*
     * Updates the details of a customer in the database.
     *
     * Will throw exception if the customer id doesnt exist.
     */
    public Customer updateCustomer(Customer newCustomer, Long customerId) {
        return customerRepository.findById(customerId)
                .map(customer -> {
                    customer.setName(newCustomer.getName());
                    customer.setLicensePlate(newCustomer.getLicensePlate());
                    customer.setTelephoneNumber(newCustomer.getTelephoneNumber());
                    return customerRepository.save(customer);
                })
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    /*
     * Deletes a customer from the database.
     *
     * Will throw exception if the customer id doesnt exist.
     */
    public void deleteCustomer(Long customerId) {
        if (customerRepository.findById(customerId).isPresent()) {
            customerRepository.deleteById(customerId);
        } else {
            throw new CustomerNotFoundException(customerId);
        }
    }
}
