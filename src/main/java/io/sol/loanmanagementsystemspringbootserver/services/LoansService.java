package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.LoanDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Loan;
import io.sol.loanmanagementsystemspringbootserver.events.LoanDisbursedEvent;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.repositories.*;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    private final LoansRepository loansRepository;
    private final FinancialStateService financialStateService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    public LoansService(LoansRepository loansRepository,
                        FinancialStateService financialStateService,
                        ApplicationEventPublisher applicationEventPublisher,
                        CustomerRepository customerRepository,
                        EmployeeRepository employeeRepository) {
        this.loansRepository = loansRepository;
        this.financialStateService = financialStateService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
    }

    public Result<List<LoanDTO>> getAllLoans() {
        return Result.success("Loans loaded successfully.", 
            loansRepository.findAll().stream().map(DTOMapper::toDTO).collect(Collectors.toList()));
    }

    public Result<LoanDTO> getLoanById(Integer id) {
        if (id == null || id <= 0) {
            return Result.invalid("A valid loan id is required.", null);
        }

        return loansRepository.findById(id)
                .map(loan -> Result.success("Loan loaded successfully.", DTOMapper.toDTO(loan)))
                .orElseGet(() -> Result.notFound("Loan not found.", null));
    }

    public Optional<Loan> getLoanEntityById(Integer id) {
        return loansRepository.findById(id);
    }

    @Transactional
    public Loan saveLoanEntity(Loan loan) {
        return loansRepository.save(loan);
    }


    /*
    @Transactional
public Result<LoanDTO> disburseLoan(...) {

    // 1. calculate current financial state

    // 2. verify liquidity

    // 3. create loan

    // 4. record disbursement

    // 5. commit everything atomically
}
    *
    *Is customer valid?
    Is employee valid?
    Is loan amount valid?
    Is available liquidity sufficient?
    Is customer eligible?
    Is loan allowed?
     */
    @Transactional
    public Result<LoanDTO> issueLoan(int customerId, LoanDTO loanDto) {
        if (loanDto.getPrincipal() == null || loanDto.getPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.invalid("Valid principal is required.", null);
        }

        if (loanDto.getTenor() <= 0) {
            return Result.invalid("Valid tenor (in days) is required.", null);
        }

        if (loanDto.getStartDate() == null || loanDto.getMaturityDate() == null) {
            return Result.invalid("Start and maturity dates are required.", null);
        }

        // Validate liquidity
        BigDecimal availableLiquidity = financialStateService.getAvailableLiquidity();
        if (loanDto.getPrincipal().compareTo(availableLiquidity) > 0) {
            return Result.invalid("Insufficient funds to disburse this loan. Available: " + availableLiquidity, loanDto);
        }

        // Validate customer
        Optional<io.sol.loanmanagementsystemspringbootserver.entities.custom.Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            return Result.notFound("Customer not found.", null);
        }

        // Validate guarantor
        if (loanDto.getGuarantorId() == null) {
            return Result.invalid("Guarantor is required.", null);
        }
        Optional<io.sol.loanmanagementsystemspringbootserver.entities.custom.Customer> guarantorOpt = customerRepository.findById(loanDto.getGuarantorId().intValue());
        if (guarantorOpt.isEmpty()) {
            return Result.notFound("Guarantor not found.", null);
        }

        // Validate field officer
        if (loanDto.getFieldOfficerId() == null) {
            return Result.invalid("Field officer is required.", null);
        }
        Optional<io.sol.loanmanagementsystemspringbootserver.entities.custom.Employee> officerOpt = employeeRepository.findById(loanDto.getFieldOfficerId());
        if (officerOpt.isEmpty()) {
            return Result.notFound("Field officer not found.", null);
        }

        Loan loan = DTOMapper.toEntity(loanDto);
        loan.setCustomer(customerOpt.get());
        loan.setGuarantor(guarantorOpt.get());
        loan.setFieldOfficer(officerOpt.get());
        loan.setStatus(io.sol.loanmanagementsystemspringbootserver.entities.Finance.LoanStatus.ACTIVE);

        // Interest calculation - principal * interestRate (total interest for the tenor)
        BigDecimal interestAmount = loan.getPrincipal().multiply(loan.getInterestRate());
        loan.setFullPayment(loan.getPrincipal().add(interestAmount));

        Loan savedLoan = loansRepository.save(loan);
        LoanDTO savedLoanDTO = DTOMapper.toDTO(savedLoan);

        // Publish event
        applicationEventPublisher.publishEvent(new LoanDisbursedEvent(
                savedLoan.getId(),
                savedLoan.getPrincipal(),
                interestAmount,
                savedLoan.getFees(),
                savedLoan.getStartDate()
        ));

        return Result.success("Successful addition of loan", savedLoanDTO);
    }


    @Transactional
    public Result<LoanDTO> updateLoan(LoanDTO loanDto) {
        return Result.notFound("Not found", new LoanDTO());
    }


    public Result<Void> deleteLoan(Integer id) {
        if (id == null || id <= 0) {
            return Result.invalid("Select a loan from the table before deleting.", null);
        }

        if (!loansRepository.existsById(id)) {
            return Result.notFound("Loan not found.", null);
        }

        loansRepository.deleteById(id);
        return Result.success("Loan deleted successfully.", null);
    }

    @Scheduled(cron = "0 0 1 * * ?") // Every day at 1 AM
    @Transactional
    public void updateOverdueLoansSurcharges() {

    }
}
