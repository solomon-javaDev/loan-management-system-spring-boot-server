package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.config.Result;
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

    public Result<Customer> createCustomer(Customer customer) {
        if (customer == null || isBlank(customer.getFirstName()) || isBlank(customer.getLastName())) {
            return Result.invalid("First name and last name are required.", null);
        }

        Customer savedCustomer = repository.save(customer);
        return Result.success("Customer saved successfully.", savedCustomer);
    }

    public Result<List<Customer>> getAllCustomers() {
        return Result.success("Customers loaded successfully.", repository.findAll());
    }

    @Transactional
    public Result<Customer> updateCustomer(Customer customer) {
        if (customer == null || customer.getId() <= 0) {
            return Result.invalid("Select a customer from the table before updating.", null);
        }

        return repository.findById(customer.getId())
                .map(existingCustomer -> {
                    existingCustomer.setEmail(customer.getEmail());
                    existingCustomer.setFirstName(customer.getFirstName());
                    existingCustomer.setLastName(customer.getLastName());
                    existingCustomer.setTelephone(customer.getTelephone());
                    existingCustomer.setOtherNames(customer.getOtherNames());
                    return Result.success("Customer updated successfully.", repository.save(existingCustomer));
                })
                .orElseGet(() -> Result.notFound("Customer not found.", null));
    }

    public Result<Void> deleteCustomer(Customer customer) {
        if (customer == null || customer.getId() <= 0) {
            return Result.invalid("Select a customer to delete.", null);
        }

        repository.deleteById(customer.getId());
        return Result.success("Customer deleted successfully.", null);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
