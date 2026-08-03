package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.LoanDTO;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoansService {

    private final LoansRepository loanRepository;
    private final CustomerRepository customerRepository;

    public LoansService(LoansRepository loanRepository, CustomerRepository customerRepository) {
        this.loanRepository = loanRepository;
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
        Loan loan = DTOMapper.toEntity(loanDto);

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
                    existingLoan.setStatus(loanDto.getStatus());
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

        return Result.success("", loan);
    }
}
