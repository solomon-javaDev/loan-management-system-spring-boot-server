package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanInstallment;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanCalculationService {

    public BigDecimal calculateProcessingFee(Loan loan) {
        if (loan == null || loan.getPrincipal() == null) {
            return BigDecimal.ZERO;
        }
        return loan.getPrincipal().multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalInterest(Loan loan) {
        if (loan == null || loan.getPrincipal() == null || loan.getInterestRate() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal annualRate = loan.getInterestRate().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        return loan.getPrincipal().multiply(annualRate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalAmountDue(Loan loan) {
        if (loan == null || loan.getPrincipal() == null) {
            return BigDecimal.ZERO;
        }
        return loan.getPrincipal().add(calculateTotalInterest(loan)).add(calculateProcessingFee(loan)).add(loan.getFees() == null ? BigDecimal.ZERO : loan.getFees());
    }

    public List<LoanInstallment> generateSchedule(Loan loan) {
        List<LoanInstallment> installments = new ArrayList<>();
        if (loan == null || loan.getPrincipal() == null || loan.getTenor() <= 0) {
            return installments;
        }

        BigDecimal principal = loan.getPrincipal();
        BigDecimal totalInterest = calculateTotalInterest(loan);
        BigDecimal principalPortion = principal.divide(new BigDecimal(loan.getTenor()), 2, RoundingMode.HALF_UP);
        BigDecimal interestPortion = totalInterest.divide(new BigDecimal(loan.getTenor()), 2, RoundingMode.HALF_UP);
        LocalDate dueDate = loan.getStartDate() == null ? LocalDate.now() : loan.getStartDate();

        for (int i = 1; i <= loan.getTenor(); i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setInstallmentNumber(i);
            installment.setDueDate(dueDate.plusMonths(i - 1));
            installment.setPrincipalPortion(principalPortion);
            installment.setInterestPortion(interestPortion);
            installment.setAmountDue(principalPortion.add(interestPortion));
            installment.setStatus(LoanStatus.PENDING);
            installments.add(installment);
        }
        return installments;
    }
}
