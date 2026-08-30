package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerCreateDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerResponseDTO;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    public Result<CustomerResponseDTO> createCustomer(CustomerCreateDTO customerDto) {
        if(isBlank(customerDto.getFirstName())){
            return Result.invalid("First name is required", null);
                    }
        if(isBlank(customerDto.getLastName())){
            return Result.invalid("Last name is required", null);
        }

        if(isBlank(customerDto.getNin())){
            return Result.invalid("NIN is required", null);
        }

        if(customerDto.getTelephone().isEmpty()){
            return Result.invalid("Telephone is required", null);
        }

        if(customerDto.getAddress().isBlank()){
            return Result.invalid("Address is required", null);
        }

        Customer customer = DTOMapper.toEntity(customerDto);
        customer.setAccountNumber(generateAccountNumber());
        Customer savedCustomer = repository.save(customer);
        return Result.success("Customer saved successfully.", DTOMapper.toResponseDTO(savedCustomer));
    }

    public List<CustomerDTO> getCustomersDueToday(){
        //Customers due today, mapped to DTOs with their aging days (max across active loans)
        LocalDate today = LocalDate.now();
        return repository.findAllCustomersDue(today).stream()
                .map(c -> {
                    CustomerDTO dto = DTOMapper.toDTO(c);
                    int aging = c.getLoans().stream()
                            .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                            .mapToInt(l -> (int) l.getAgingDays(today))
                            .max().orElse(0);
                    dto.setAgingDays(aging);
                    return dto;
                })
                .toList();
    }

    public String generateAccountNumber() {
        long count = repository.countAllIncludingDeleted();
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
    public Result<List<CustomerDTO>> getEligibleGuarantors(BigDecimal principal) {
        List<CustomerDTO> eligible = repository.findAll().stream()
                .filter(c -> c.canGuarantee(principal))
                .map(DTOMapper::toDTO)
                .collect(Collectors.toList());
        return Result.success("Eligible guarantors loaded.", eligible);
    }

    @Transactional
    public Result<CustomerResponseDTO> updateCustomer(CustomerDTO customerDto) {
        if (customerDto == null || customerDto.getId() <= 0) {
            return Result.invalid("Select a customer from the table before updating.", null);
        }

        return repository.findById(customerDto.getId())
                .map(existingCustomer -> {
                    existingCustomer.setNin(customerDto.getNin());
                    existingCustomer.setFirstName(customerDto.getFirstName());
                    existingCustomer.setLastName(customerDto.getLastName());
                    existingCustomer.setTelephone(customerDto.getTelephone());
                    existingCustomer.setOtherNames(customerDto.getOtherNames());
                    existingCustomer.setAddress(customerDto.getAddress());
                    existingCustomer.setSavingsBalance(customerDto.getSavingsBalance());
                    existingCustomer.setActive(customerDto.isActive());
                    if (customerDto.getFieldOfficer() != null) {
                        existingCustomer.setFieldOfficer(DTOMapper.toEntity(customerDto.getFieldOfficer()));
                    } else {
                        existingCustomer.setFieldOfficer(null);
                    }
                    Customer saved = repository.save(existingCustomer);
                    return Result.success("Customer updated successfully.", DTOMapper.toResponseDTO(saved));
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

    public Result<CustomerDTO> getCustomerByNin(String nin) {
        return repository.findByNin(nin)
                .map(DTOMapper::toDTO)
                .map(dto -> Result.success("Customer found.", dto))
                .orElseGet(() -> Result.notFound("Customer with NIN " + nin + " not found.", new CustomerDTO()));
    }

    public Result<Customer> restoreCustomer(int id){
        //TODO I have to add to the view a choice for restoring a deleted customer
        //TODO That will require as well adding methods to query the soft deleted customers
        Optional<Customer> customer = repository.findById(id);

        if(customer.isPresent()){
            customer.get().setDeleted(true);
            return Result.success("Customer restored", customer.get());

        }

        return Result.notFound("Customer is not existing", null);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
