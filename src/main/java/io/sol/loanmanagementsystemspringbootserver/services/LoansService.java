package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.LoanDTO;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.entities.*;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailDetails;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailsService;
import io.sol.loanmanagementsystemspringbootserver.repositories.*;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final LoanParameterChangeRepository changeRepository;
    private final EmailsService emailsService;
    private final SystemSettingService settingService;

    public LoansService(LoansRepository loanRepository, 
                        CustomerRepository customerRepository, 
                        EmployeeRepository employeeRepository,
                        LoanParameterChangeRepository changeRepository,
                        EmailsService emailsService,
                        SystemSettingService settingService) {
        this.loanRepository = loanRepository;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.changeRepository = changeRepository;
        this.emailsService = emailsService;
        this.settingService = settingService;
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

        if (loanDto.getGuarantorId() == null) {
            return Result.invalid("A guarantor is required for this loan.", null);
        }

        Optional<Customer> guarantorOptional = customerRepository.findById(Math.toIntExact(loanDto.getGuarantorId()));
        if (guarantorOptional.isEmpty()) {
            return Result.notFound("Selected guarantor not found.", null);
        }

        Customer guarantor = guarantorOptional.get();
        if (guarantor.getId() == customer.getId()) {
            return Result.invalid("The guarantor cannot be the same customer taking the loan.", null);
        }

        Result<LoanDTO> guarantorValidation = validateGuarantorExposure(customer, guarantor, loanDto.getPrincipal());
        if (guarantorValidation.isFailure()) {
            return guarantorValidation;
        }

        Loan loan = DTOMapper.toEntity(loanDto);
        if (loanDto.getFieldOfficerId() != null) {
            Optional<Employee> officer = employeeRepository.findById(loanDto.getFieldOfficerId());
            if (officer.isPresent()) {
                loan.setFieldOfficer(officer.get());
            }
        }
        loan.setCustomer(customer);
        loan.setGuarantor(guarantor);

        // 3. Sync the bidirectional object graph
        customer.addLoan(loan);

        // 4. Explicitly persist the loan entity
        Loan savedLoan = loanRepository.save(loan);

        return Result.success("Loan attached to customer successfully.", DTOMapper.toDTO(savedLoan));
    }


    @Transactional
    public Result<LoanDTO> updateLoan(LoanDTO loanDto) {
        if (loanDto == null || loanDto.getId() <= 0) {
            return Result.invalid("Select a loan from the table before updating.", null);
        }

        Result<LoanDTO> validationResult = validateLoan(loanDto);
        if (validationResult.isFailure()) {
            return validationResult;
        }

        if (loanDto.getGuarantorId() == null) {
            return Result.invalid("A guarantor is required for this loan.", null);
        }

        return (Result<LoanDTO>) loanRepository.findById(loanDto.getId())
                .map(existingLoan -> {
                    Optional<Customer> guarantorOptional = customerRepository.findById(Math.toIntExact(loanDto.getGuarantorId()));
                    if (guarantorOptional.isEmpty()) {
                        return Result.notFound("Selected guarantor not found.", null);
                    }

                    Customer guarantor = guarantorOptional.get();
                    if (existingLoan.getCustomer() != null && guarantor.getId() == existingLoan.getCustomer().getId()) {
                        return Result.invalid("The guarantor cannot be the same customer taking the loan.", null);
                    }

                    Result<LoanDTO> guarantorValidation = validateGuarantorExposure(existingLoan.getCustomer(), guarantor, loanDto.getPrincipal());
                    if (guarantorValidation.isFailure()) {
                        return guarantorValidation;
                    }

                    List<LoanParameterChange> changes = new ArrayList<>();
                    
                    trackChange(changes, existingLoan, "startDate", existingLoan.getStartDate(), loanDto.getStartDate());
                    trackChange(changes, existingLoan, "principal", existingLoan.getPrincipal(), loanDto.getPrincipal());
                    trackChange(changes, existingLoan, "interestRate", existingLoan.getInterestRate(), loanDto.getInterestRate());
                    trackChange(changes, existingLoan, "fees", existingLoan.getFees(), loanDto.getFees());
                    trackChange(changes, existingLoan, "tenor", existingLoan.getTenor(), loanDto.getTenor());

                    existingLoan.setStartDate(loanDto.getStartDate());
                    existingLoan.setMaturityDate(loanDto.getMaturityDate());
                    existingLoan.setFullPaidDate(loanDto.getFullPaidDate());
                    existingLoan.setPrincipal(loanDto.getPrincipal());
                    existingLoan.setInterestRate(loanDto.getInterestRate());
                    existingLoan.setTenor(loanDto.getTenor());
                    existingLoan.setCollateral(loanDto.getCollateral());
                    existingLoan.setFees(loanDto.getFees());
                    existingLoan.setStatus(loanDto.getStatus() != null ? loanDto.getStatus() : LoanStatus.PENDING);
                    existingLoan.setGuarantor(guarantor);

                    if (loanDto.getFieldOfficerId() != null) {
                        Optional<Employee> officer = employeeRepository.findById(loanDto.getFieldOfficerId());
                        officer.ifPresent(existingLoan::setFieldOfficer);
                    }
                    
                    Loan saved = loanRepository.save(existingLoan);
                    if (!changes.isEmpty()) {
                        changeRepository.saveAll(changes);
                        sendAdminAlert(saved, changes);
                    }
                    
                    return Result.success("Loan updated successfully.", DTOMapper.toDTO(saved));
                })
                .orElseGet(() -> Result.notFound("Loan not found.", null));
    }

    private Result<LoanDTO> validateGuarantorExposure(Customer customer, Customer guarantor, BigDecimal loanPrincipal) {
        if (guarantor == null) {
            return Result.invalid("A guarantor is required for this loan.", null);
        }

        if (customer != null && customer.getId() == guarantor.getId()) {
            return Result.invalid("The guarantor cannot be the same customer taking the loan.", null);
        }

        if (!guarantor.canGuarantee(loanPrincipal)) {
            return Result.invalid(
                    "Guarantor is not eligible: they must have no outstanding loan, or their savings must cover their own loan(s) and this new loan.",
                    null
            );
        }

        return Result.success("", null);
    }

    private void trackChange(List<LoanParameterChange> changes, Loan loan, String param, Object oldVal, Object newVal) {
        if (oldVal == null && newVal == null) return;
        if (oldVal != null && oldVal.equals(newVal)) return;
        
        LoanParameterChange change = new LoanParameterChange();
        change.setLoan(loan);
        change.setParameterName(param);
        change.setOldValue(String.valueOf(oldVal));
        change.setNewValue(String.valueOf(newVal));
        change.setChangedBy("System/User"); // In a real system, get from security context
        changes.add(change);
    }

    private void sendAdminAlert(Loan loan, List<LoanParameterChange> changes) {
        String adminEmails = settingService.getSetting("report.emails", "");
        if (adminEmails.isBlank()) return;

        StringBuilder sb = new StringBuilder("Loan Parameter Change Alert\n\n");
        sb.append("Loan ID: ").append(loan.getId()).append("\n");
        sb.append("Customer: ").append(loan.getCustomer().getFirstName()).append(" ").append(loan.getCustomer().getLastName()).append("\n\n");
        sb.append("Changes:\n");
        for (LoanParameterChange c : changes) {
            sb.append("- ").append(c.getParameterName()).append(": ").append(c.getOldValue()).append(" -> ").append(c.getNewValue()).append("\n");
        }

        EmailDetails details = new EmailDetails();
        details.setRecipient(adminEmails);
        details.setSubject("ALERT: Loan Parameter Change - ID " + loan.getId());
        details.setBody(sb.toString());
        emailsService.sendSimpleMail(details);
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
    @Scheduled(cron = "0 0 1 * * ?") // Every day at 1 AM
    @Transactional
    public void updateOverdueLoansSurcharges() {
        LocalDate today = LocalDate.now();
        List<Loan> activeLoans = loanRepository.findByStatusIn(Arrays.asList(LoanStatus.ACTIVE, LoanStatus.DEFAULTED));

        BigDecimal configuredRate;
        try {
            configuredRate = new BigDecimal(settingService.getSetting("loan.surcharge.rate", "0"))
                    .divide(BigDecimal.valueOf(100), 10, RoundingMode.DOWN);
        } catch (NumberFormatException e) {
            configuredRate = BigDecimal.ZERO;
        }

        for (Loan loan : activeLoans) {
            if (loan.getMaturityDate() != null && today.isAfter(loan.getMaturityDate())) {
                loan.setSurchargeRate(configuredRate);
                BigDecimal surcharge = loan.calculateSurcharge(today);
                if (surcharge.compareTo(BigDecimal.ZERO) > 0) {
                    loan.setSurchargeAmount(surcharge);
                    if (loan.getStatus() == LoanStatus.ACTIVE) {
                        loan.setStatus(LoanStatus.DEFAULTED);
                    }
                    loanRepository.save(loan);
                }
            }
        }
    }
}
