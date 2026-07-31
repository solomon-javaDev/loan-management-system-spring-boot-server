package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.entities.Payment;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class PaymentsService {

    private final LoansService loansService;

    public PaymentsService(LoansService loansService) {
        this.loansService = loansService;
    }

    public Result<Payment> savePayment(LocalDate date, BigDecimal amountReceived, int loanID){
        Loan loan = loansService.getLoanById(loanID).value();

        if(amountReceived.compareTo(BigDecimal.ZERO) <= 0){
            return Result.invalid("Amount cannot be zero or less", null);
        }

        Payment payment = new Payment();

        payment.setLoan(loan);
        payment.setDate(date);
        payment.setAmountReceived(amountReceived);

        if(loan.getStatus() == LoanStatus.PENDING && loan.getTotalPaid().compareTo(BigDecimal.ZERO) >0 ){
            loan.setStatus(LoanStatus.ACTIVE);
        }

        if(loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0){
            loan.setStatus(LoanStatus.CLOSED);
        }

        return Result.success("Successfull payment",payment);

    }

//    public Result<Boolean> deletePayment(Payment payment, int loanId){
//        return  Result.success("Payment deleted",
//        loansService.getLoanById(loanId).value().getPayments().remove(payment));
//    }
//
//    public Result<Payment> updatePayment(Payment payment, int loanId){
//        Loan loan = loansService.getLoanById(loanId).value();
//        Payment p = loan.getPayments().get(payment);
//    }


}
