package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.config.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.*;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoanInstallmentRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.RepaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class RepaymentService {

    private final LoansRepository loanRepository;
    private final LoanInstallmentRepository installmentRepository;
    private final RepaymentRepository repaymentRepository;

    public RepaymentService(LoansRepository loanRepository, LoanInstallmentRepository installmentRepository,
                            RepaymentRepository repaymentRepository) {
        this.loanRepository = loanRepository;
        this.installmentRepository = installmentRepository;
        this.repaymentRepository = repaymentRepository;
    }

    @Transactional
    public Result<Repayment> applyPayment(Integer loanId, Integer installmentId, BigDecimal amount, String receivedBy) {
        if (loanId == null || installmentId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.invalid("A valid loan, installment, and payment amount are required.", null);
        }

        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        Optional<LoanInstallment> installmentOptional = installmentRepository.findById(installmentId);
        if (loanOptional.isEmpty() || installmentOptional.isEmpty()) {
            return Result.notFound("Loan or installment not found.", null);
        }

        Loan loan = loanOptional.get();
        LoanInstallment installment = installmentOptional.get();
        if (amount.compareTo(installment.getAmountDue().subtract(installment.getPaidAmount())) > 0) {
            return Result.invalid("Overpayment is not allowed for this installment.", null);
        }

        installment.setPaidAmount(installment.getPaidAmount().add(amount));
        if (installment.getPaidAmount().compareTo(installment.getAmountDue()) >= 0) {
            installment.setStatus(LoanStatus.CLOSED);
        } else {
            installment.setStatus(LoanStatus.ACTIVE);
        }
        installmentRepository.save(installment);

        loan.setTotalPaid(loan.getTotalPaid().add(amount));
        loan.setOutstandingPrincipal(loan.getOutstandingPrincipal().subtract(amount.min(loan.getOutstandingPrincipal())));
        loan.setOutstandingInterest(loan.getOutstandingInterest().subtract(amount.min(loan.getOutstandingInterest())));
        if (loan.getOutstandingPrincipal().compareTo(BigDecimal.ZERO) <= 0 && loan.getOutstandingInterest().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setFullPaidDate(LocalDate.now());
        } else {
            loan.setStatus(loan.getStatus() == LoanStatus.PENDING ? LoanStatus.ACTIVE : loan.getStatus());
        }
        loanRepository.save(loan);

        Repayment repayment = new Repayment();
        repayment.setLoan(loan);
        repayment.setInstallment(installment);
        repayment.setAmount(amount);
        repayment.setPaymentDate(LocalDate.now());
        repayment.setReceivedBy(receivedBy);
        repayment.setReceiptNumber("RCPT-" + System.currentTimeMillis());
        return Result.success("Payment applied successfully.", repaymentRepository.save(repayment));
    }
}
