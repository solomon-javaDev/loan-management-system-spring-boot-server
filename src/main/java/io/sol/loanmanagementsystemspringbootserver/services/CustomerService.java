package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerCreateDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerResponseDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.Customer;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UserSession;
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
    private final UserSession userSession;
    private final AuditService auditService;

    @Autowired
    public CustomerService(CustomerRepository repository, UserSession userSession, AuditService auditService) {
        this.repository = repository;
        this.userSession = userSession;
        this.auditService = auditService;
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

//    public List<CustomerDTO> getCustomersDueToday(){
//
//    }

    public String generateAccountNumber() {
        long count = repository.countAllIncludingDeleted();
        return String.format("055%08d", count + 1);
    }

    public Result<CustomerDTO> getCustomerById(int id) {
        return repository.findById(id)
                .map(DTOMapper::toDTO)
                .map(dto -> Result.success("Customer found.", dto))
                .orElseGet(() -> Result.notFound("Customer not found.", null));
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
    public Result<CustomerResponseDTO> updateCustomer(CustomerDTO customerDto) {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
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
                    existingCustomer.setActive(customerDto.isActive());
                    if (customerDto.getFieldOfficer() != null) {
                        existingCustomer.setFieldOfficer(DTOMapper.toEntity(customerDto.getFieldOfficer()));
                    } else {
                        existingCustomer.setFieldOfficer(null);
                    }
                    Customer saved = repository.save(existingCustomer);
                    auditService.record("UPDATE", "CUSTOMER", saved.getId(), "Customer details updated");
                    return Result.success("Customer updated successfully.", DTOMapper.toResponseDTO(saved));
                })
                .orElseGet(() -> Result.notFound("Customer not found.", null));
    }

    public Result<Void> deleteCustomer(CustomerDTO customerDto) {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        if (customerDto == null || customerDto.getId() <= 0) {
            return Result.invalid("Select a customer to delete.", null);
        }

        repository.deleteById(customerDto.getId());
        auditService.record("SOFT_DELETE", "CUSTOMER", customerDto.getId(), "Customer soft deleted");
        return Result.success("Customer deleted successfully.", null);
    }

    public Result<CustomerDTO> getCustomerByNin(String nin) {
        return repository.findByNin(nin)
                .map(DTOMapper::toDTO)
                .map(dto -> Result.success("Customer found.", dto))
                .orElseGet(() -> Result.notFound("Customer with NIN " + nin + " not found.", new CustomerDTO()));
    }

    public Result<Customer> restoreCustomer(int id){
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        if (repository.restoreById(id) == 0) {
            return Result.notFound("Customer is not existing", null);
        }
        auditService.record("RESTORE", "CUSTOMER", id, "Customer restored");
        return repository.findById(id)
                .map(customer -> Result.success("Customer restored", customer))
                .orElseGet(() -> Result.notFound("Customer could not be loaded after restore", null));
    }

    public Result<List<Customer>> getDeletedCustomers() {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        return Result.success("Deleted customers loaded", repository.findAllDeleted());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
