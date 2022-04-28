package nl.bd.garage.services;

import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.exceptions.CustomerNotFoundException;
import nl.bd.garage.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(long customerId) {
        return customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    public Customer createCustomer(Customer newCustomer) {
        return customerRepository.save(newCustomer);
    }

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

    public void deleteCustomer(Long customerId) {
        if (customerRepository.findById(customerId).isPresent()) {
            customerRepository.deleteById(customerId);
        } else {
            throw new CustomerNotFoundException(customerId);
        }
    }
}
