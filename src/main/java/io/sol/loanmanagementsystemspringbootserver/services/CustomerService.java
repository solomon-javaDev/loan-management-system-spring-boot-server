package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    @Autowired
    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public Customer createCustomer(Customer customer) {
        return repository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    @Transactional
    public Customer updateCustomer(Customer customer) {
        return repository.findById(customer.getId())
                .map(existingCustomer -> {
                    existingCustomer.setEmail(customer.getEmail());
                    existingCustomer.setFirstName(customer.getFirstName());
                    existingCustomer.setLastName(customer.getLastName());
                    existingCustomer.setTelephone(customer.getTelephone());
                    existingCustomer.setOtherNames(customer.getOtherNames());
                    return existingCustomer;
                })
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    public void deleteCustomer(Customer customer) {
        repository.deleteById(customer.getId());
    }
}
