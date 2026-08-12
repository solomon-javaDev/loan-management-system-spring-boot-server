package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.LoanDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.PaymentDTO;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.entities.Payment;
import io.sol.loanmanagementsystemspringbootserver.repositories.PaymentRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class responsible for handling payment operations and related functionalities.
 * Manages interactions with the PaymentRepository and LoansService to perform CRUD operations,
 * update loan statuses, and retrieve payment data.
 */
@Service
public class PaymentsService {

    private final PaymentRepository paymentRepository;
    private final LoansService loansService;

    public PaymentsService(LoansService loansService, PaymentRepository paymentRepository) {
        this.loansService = loansService;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Result<PaymentDTO> savePayment(LocalDate date, BigDecimal amountReceived, int loanID){
        if(amountReceived == null || amountReceived.compareTo(BigDecimal.ZERO) <= 0){
            return Result.invalid("Amount must be greater than zero", null);
        }

        return loansService.getLoanEntityById(loanID).map(loan -> {
             Payment payment = new Payment();
             payment.setLoan(loan);
             payment.setDate(date != null ? date : LocalDate.now());
             payment.setAmountReceived(amountReceived);
             loan.addPayment(payment);

             Payment savedPayment = paymentRepository.save(payment);
        
             updateLoanStatus(loan, savedPayment.getDate());
             loansService.saveLoanEntity(loan);

             String message = "Successful payment" + (loan.getStatus() == LoanStatus.CLOSED ? ", loan fully paid and cleared." : "");
             return Result.success(message, DTOMapper.toDTO(savedPayment));
        }).orElse(Result.notFound("Loan not found", null));
    }

    @Transactional
    public Result<PaymentDTO> updatePayment(int paymentId, LocalDate date, BigDecimal amountReceived) {
        return paymentRepository.findById(paymentId).map(payment -> {
            payment.setDate(date);
            payment.setAmountReceived(amountReceived);
            Payment updated = paymentRepository.save(payment);
            
            updateLoanStatus(payment.getLoan(), updated.getDate());
            loansService.saveLoanEntity(payment.getLoan());
            
            return Result.success("Payment updated successfully", DTOMapper.toDTO(updated));
        }).orElse(Result.notFound("Payment not found", null));
    }

    @Transactional
    public Result<Void> deletePayment(int paymentId) {
        return paymentRepository.findById(paymentId).map(payment -> {
            Loan loan = payment.getLoan();
            loan.removePayment(payment);
            paymentRepository.delete(payment);
            
            updateLoanStatus(loan, getLastPaymentDate(loan));
            loansService.saveLoanEntity(loan);
            
            return Result.success("Payment deleted successfully", (Void) null);
        }).orElse(Result.notFound("Payment not found", null));
    }

    private LocalDate getLastPaymentDate(Loan loan) {
        return loan.getPayments().stream()
                .map(Payment::getDate)
                .filter(date -> date != null)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Result<List<PaymentDTO>> getAllPayments() {
        List<Payment> payments = paymentRepository.findAllByOrderByDateDesc();
        return Result.success("Payments loaded successfully", 
            payments.stream().map(DTOMapper::toDTO).collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public Result<List<PaymentDTO>> getPaymentsByDate(LocalDate date) {
        return Result.success("Payments for date loaded", 
            paymentRepository.findByDate(date).stream().map(DTOMapper::toDTO).collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public Result<List<PaymentDTO>> getPaymentsBetween(LocalDate start, LocalDate end) {
        return Result.success("Payments between dates loaded", 
            paymentRepository.findByDateBetween(start, end).stream().map(DTOMapper::toDTO).collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public Result<List<PaymentDTO>> getPaymentsByLoan(int loanId) {
        return loansService.getLoanEntityById(loanId).map(loan -> 
             Result.success("Payments for loan loaded", 
                loan.getPayments().stream().map(p -> DTOMapper.toDTO(p)).collect(Collectors.toList()))
        ).orElse(Result.notFound("Loan not found", null));
    }

    @Transactional(readOnly = true)
    protected void updateLoanStatus(Loan loan, LocalDate lastPaymentDate) {
        if (loan.getTotalPaid().compareTo(BigDecimal.ZERO) > 0 && loan.getStatus() == LoanStatus.PENDING) {
            loan.setStatus(LoanStatus.ACTIVE);
        }

        if (loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setFullPayment(loan.getTotalPaid());
            loan.setFullPaidDate(lastPaymentDate != null ? lastPaymentDate : LocalDate.now());
        } else {
            if (loan.getStatus() == LoanStatus.CLOSED) {
                loan.setStatus(LoanStatus.ACTIVE);
            }
            loan.setFullPayment(null);
            loan.setFullPaidDate(null);
        }
    }
}
