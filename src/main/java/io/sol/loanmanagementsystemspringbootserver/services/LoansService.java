package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.LoanDTO;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.repositories.EmployeeRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.entities.Employee;
import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class that provides operations for managing loans and their associations
 * with customers. The class interacts with the underlying data repositories to
 * perform CRUD operations on loan entities and provides functionality such as
 * issuing loans, updating loan details, and validating input data.
 */
@Service
public class LoansService {

    private final LoansRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    public LoansService(LoansRepository loanRepository, CustomerRepository customerRepository, EmployeeRepository employeeRepository) {
        this.loanRepository = loanRepository;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
    }

    public Result<List<LoanDTO>> getAllLoans() {
        return Result.success("Loans loaded successfully.", 
            loanRepository.findAll().stream().map(DTOMapper::toDTO).collect(Collectors.toList()));
    }

    public Result<LoanDTO> getLoanById(Integer id) {
        if (id == null || id <= 0) {
            return Result.invalid("A valid loan id is required.", null);
        }

        return loanRepository.findById(id)
                .map(loan -> Result.success("Loan loaded successfully.", DTOMapper.toDTO(loan)))
                .orElseGet(() -> Result.notFound("Loan not found.", null));
    }

    public Optional<Loan> getLoanEntityById(Integer id) {
        return loanRepository.findById(id);
    }

    @Transactional
    public Loan saveLoanEntity(Loan loan) {
        return loanRepository.save(loan);
    }


    @Transactional
    public Result<LoanDTO> issueLoan(int customerId, LoanDTO loanDto) {
        // 1. Validate the loan object properties upfront
        Result<LoanDTO> validationResult = validateLoan(loanDto);
        if (validationResult.isFailure()) {
            return validationResult;
        }

        // 2. Locate the existing customer
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isEmpty()) {
            return Result.notFound("Customer not found. Please create or select a valid customer first.", null);
        }

        Customer customer = customerOptional.get();

        boolean hasExistingLoan = customer.getLoans().stream()
                .anyMatch(existingLoan -> existingLoan.getStatus() == LoanStatus.ACTIVE || existingLoan.getStatus() == LoanStatus.PENDING);
        if (hasExistingLoan) {
            return Result.invalid("This customer already has an active or pending loan. Issue a new loan only after the existing loan is settled or closed.", null);
        }

        Loan loan = DTOMapper.toEntity(loanDto);
        if (loanDto.getFieldOfficerId() != null) {
            Optional<Employee> officer = employeeRepository.findById(loanDto.getFieldOfficerId());
            if (officer.isPresent()) {
                loan.setFieldOfficer(officer.get());
            }
        }

        // 3. Sync the bidirectional object graph
        customer.addLoan(loan);

        // 4. Explicitly persist the loan entity
        Loan savedLoan = loanRepository.save(loan);

        return Result.success("Loan attached to customer successfully.", DTOMapper.toDTO(savedLoan));
    }


    public Result<LoanDTO> updateLoan(LoanDTO loanDto) {
        if (loanDto == null || loanDto.getId() <= 0) {
            return Result.invalid("Select a loan from the table before updating.", null);
        }

        Result<LoanDTO> validationResult = validateLoan(loanDto);
        if (validationResult.isFailure()) {
            return validationResult;
        }

        return loanRepository.findById(loanDto.getId())
                .map(existingLoan -> {
                    existingLoan.setStartDate(loanDto.getStartDate());
                    existingLoan.setMaturityDate(loanDto.getMaturityDate());
                    existingLoan.setFullPaidDate(loanDto.getFullPaidDate());
                    existingLoan.setPrincipal(loanDto.getPrincipal());
                    existingLoan.setInterestRate(loanDto.getInterestRate());
                    existingLoan.setTenor(loanDto.getTenor());
                    existingLoan.setCollateral(loanDto.getCollateral());
                    existingLoan.setFees(loanDto.getFees());
                    existingLoan.setStatus(loanDto.getStatus() != null ? loanDto.getStatus() : LoanStatus.PENDING);

                    if (loanDto.getFieldOfficerId() != null) {
                        Optional<Employee> officer = employeeRepository.findById(loanDto.getFieldOfficerId());
                        officer.ifPresent(existingLoan::setFieldOfficer);
                    }
                    // Relations update if needed, but usually IDs stay the same
                    return Result.success("Loan updated successfully.", DTOMapper.toDTO(loanRepository.save(existingLoan)));
                })
                .orElseGet(() -> Result.notFound("Loan not found.", null));
    }

    public Result<Void> deleteLoan(Integer id) {
        if (id == null || id <= 0) {
            return Result.invalid("Select a loan from the table before deleting.", null);
        }

        if (!loanRepository.existsById(id)) {
            return Result.notFound("Loan not found.", null);
        }

        loanRepository.deleteById(id);
        return Result.success("Loan deleted successfully.", null);
    }

    private static Result<LoanDTO> validateLoan(LoanDTO loan) {
        if (loan == null) {
            return Result.invalid("Loan details are required.", null);
        }

        if (loan.getPrincipal() == null || loan.getPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.invalid("Principal must be greater than zero.", null);
        }

        if (loan.getInterestRate() == null || loan.getInterestRate().compareTo(BigDecimal.ZERO) < 0) {
            return Result.invalid("Interest rate cannot be negative.", null);
        }

        if (loan.getStartDate() == null || loan.getMaturityDate() == null) {
            return Result.invalid("Start date and maturity date are required.", null);
        }

        if (loan.getMaturityDate().isBefore(loan.getStartDate())) {
            return Result.invalid("Maturity date cannot be earlier than the start date.", null);
        }

        if (loan.getTenor() < 0) {
            return Result.invalid("Tenor cannot be negative. Check your maturity date.", null);
        }

        if (loan.getStatus() == null) {
            loan.setStatus(LoanStatus.PENDING);
        }

        if (loan.getFieldOfficerId() == null || loan.getFieldOfficerName() == null || loan.getFieldOfficerName().isBlank()) {
            return Result.invalid("Field Officer required", null);
        }

        return Result.success("", loan);
    }
}
