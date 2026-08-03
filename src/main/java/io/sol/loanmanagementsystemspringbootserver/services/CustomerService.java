package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The CustomerService class provides services for managing customer operations
 * such as creating, retrieving, updating, and deleting customer records.
 * It interacts with the data repository and performs operations on Customer entities.
 */

@Service
public class CustomerService {

    private final CustomerRepository repository;

    @Autowired
    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public Result<CustomerDTO> createCustomer(CustomerDTO customerDto) {
        if (customerDto == null || isBlank(customerDto.getFirstName()) || isBlank(customerDto.getLastName())) {
            return Result.invalid("First name and last name are required.", null);
        }

        Customer customer = DTOMapper.toEntity(customerDto);
        customer.setAccountNumber(generateAccountNumber());
        Customer savedCustomer = repository.save(customer);
        return Result.success("Customer saved successfully.", DTOMapper.toDTO(savedCustomer));
    }

    private String generateAccountNumber() {
        long count = repository.count();
        return String.format("055%08d", count + 1);
    }

    public Result<List<CustomerDTO>> getAllCustomers() {
        List<Object[]> results = repository.findAllWithLoanCount();
        List<CustomerDTO> dtos = results.stream().map(row -> {
            Customer c = (Customer) row[0];
            Long count = (Long) row[1];
            CustomerDTO dto = DTOMapper.toDTO(c);
            dto.setLoanCount(count.intValue());
            return dto;
        }).collect(Collectors.toList());
        return Result.success("Customers loaded successfully.", dtos);
    }

    @Transactional
    public Result<CustomerDTO> updateCustomer(CustomerDTO customerDto) {
        if (customerDto == null || customerDto.getId() <= 0) {
            return Result.invalid("Select a customer from the table before updating.", null);
        }

        return repository.findById(customerDto.getId())
                .map(existingCustomer -> {
                    existingCustomer.setEmail(customerDto.getEmail());
                    existingCustomer.setFirstName(customerDto.getFirstName());
                    existingCustomer.setLastName(customerDto.getLastName());
                    existingCustomer.setTelephone(customerDto.getTelephone());
                    existingCustomer.setOtherNames(customerDto.getOtherNames());
                    existingCustomer.setAddress(customerDto.getAddress());
                    Customer saved = repository.save(existingCustomer);
                    return Result.success("Customer updated successfully.", DTOMapper.toDTO(saved));
                })
                .orElseGet(() -> Result.notFound("Customer not found.", null));
    }

    public Result<Void> deleteCustomer(CustomerDTO customerDto) {
        if (customerDto == null || customerDto.getId() <= 0) {
            return Result.invalid("Select a customer to delete.", null);
        }

        repository.deleteById(customerDto.getId());
        return Result.success("Customer deleted successfully.", null);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
