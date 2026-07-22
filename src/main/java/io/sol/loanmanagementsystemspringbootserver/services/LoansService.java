package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.config.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class LoansService {

    private final LoansRepository loanRepository;
    private final CustomerRepository customerRepository;

    public LoansService(LoansRepository loanRepository, CustomerRepository customerRepository) {
        this.loanRepository = loanRepository;
        this.customerRepository = customerRepository;
    }

    public Result<List<Loan>> getAllLoans() {
        return Result.success("Loans loaded successfully.", loanRepository.findAll());
    }

    public Result<Loan> getLoanById(Integer id) {
        if (id == null || id <= 0) {
            return Result.invalid("A valid loan id is required.", null);
        }

        return loanRepository.findById(id)
                .map(loan -> Result.success("Loan loaded successfully.", loan))
                .orElseGet(() -> Result.notFound("Loan not found.", null));
    }

    public Result<Loan> createLoan(Loan loan) {
        Result<Loan> validationResult = validateLoan(loan);
        if (validationResult.isFailure()) {
            return validationResult;
        }

        Loan savedLoan = loanRepository.save(loan);
        return Result.success("Loan saved successfully.", savedLoan);
    }
    @Transactional
    public Result<Loan> issueLoan(int customerId, Loan loan) {
        // 1. Validate the loan object properties upfront to save database roundtrips
        Result<Loan> validationResult = validateLoan(loan);
        if (validationResult.isFailure()) {
            return validationResult;
        }

        // 2. Locate the existing customer or return a descriptive failure payload
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isEmpty()) {
            return Result.notFound("Customer not found. Please create or select a valid customer first.", null);
        }

        Customer customer = customerOptional.get();

        // 3. Sync the bidirectional object graph using entity helper method
        customer.addLoan(loan);

        // 4. Explicitly persist the loan entity and store its database-generated ID
        Loan savedLoan = loanRepository.save(loan);

        return Result.success("Loan attached to customer successfully.", savedLoan);
    }


    public Result<Loan> updateLoan(Loan loan) {
        if (loan == null || loan.getId() <= 0) {
            return Result.invalid("Select a loan from the table before updating.", null);
        }

        Result<Loan> validationResult = validateLoan(loan);
        if (validationResult.isFailure()) {
            return validationResult;
        }

        return loanRepository.findById(loan.getId())
                .map(existingLoan -> {
                    existingLoan.setStartDate(loan.getStartDate());
                    existingLoan.setMaturityDate(loan.getMaturityDate());
                    existingLoan.setFullPaidDate(loan.getFullPaidDate());
                    existingLoan.setPrincipal(loan.getPrincipal());
                    existingLoan.setInterestRate(loan.getInterestRate());
                    existingLoan.setTenor(loan.getTenor());
                    existingLoan.setCollateral(loan.getCollateral());
                    existingLoan.setFees(loan.getFees());
                    existingLoan.setStatus(loan.getStatus());
                    existingLoan.setFieldOfficer(loan.getFieldOfficer());
                    existingLoan.setGuarantor(loan.getGuarantor());
                    existingLoan.setCustomer(loan.getCustomer());
                    return Result.success("Loan updated successfully.", loanRepository.save(existingLoan));
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

    private static Result<Loan> validateLoan(Loan loan) {
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
