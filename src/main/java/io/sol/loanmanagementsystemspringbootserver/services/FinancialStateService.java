package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.SystemFinancialState;
import io.sol.loanmanagementsystemspringbootserver.events.FinancialStateUpdatedEvent;
import io.sol.loanmanagementsystemspringbootserver.repositories.SystemFinancialStateRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The class that performs all operations to change the system state
 */
@Service
public class FinancialStateService {


    private final SystemFinancialStateRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public FinancialStateService(SystemFinancialStateRepository systemFinancialStateRepository, ApplicationEventPublisher eventPublisher) {
        this.repository = systemFinancialStateRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void applyLoanDisbursement(BigDecimal principal, BigDecimal interest, BigDecimal fees) {
        SystemFinancialState state = getCurrentState();

        // Cash moves: subtract principal, add fees (since they are deducted from disbursement)
        BigDecimal netCashOut = principal.subtract(fees);
        state.setCashOnHand(state.getCashOnHand().subtract(netCashOut));

        state.setTotalCashDisbursedInLoans(state.getTotalCashDisbursedInLoans().add(principal));
        state.setOutstandingPrincipal(state.getOutstandingPrincipal().add(principal));
        state.setGrossLoanPortfolio(state.getOutstandingPrincipal());

        state.setTotalInterestCharged(state.getTotalInterestCharged().add(interest));
        state.setInterestReceivable(state.getInterestReceivable().add(interest));

        state.setTotalFeesCollected(state.getTotalFeesCollected().add(fees));

        updateDerivedValues(state);
        repository.save(state);
        eventPublisher.publishEvent(new FinancialStateUpdatedEvent(state));
    }

    @Transactional
    public void applyRepayment(BigDecimal principal, BigDecimal interest, BigDecimal fees, BigDecimal surcharge) {
        SystemFinancialState state = getCurrentState();

        BigDecimal totalPayment = principal.add(interest).add(fees).add(surcharge);
        state.setCashOnHand(state.getCashOnHand().add(totalPayment));

        state.setTotalPrincipalCollected(state.getTotalPrincipalCollected().add(principal));
        state.setOutstandingPrincipal(state.getOutstandingPrincipal().subtract(principal));
        state.setGrossLoanPortfolio(state.getOutstandingPrincipal());

        state.setInterestReceived(state.getInterestReceived().add(interest));
        state.setInterestReceivable(state.getInterestReceivable().subtract(interest));

        state.setTotalFeesCollected(state.getTotalFeesCollected().add(fees));
        state.setTotalSurchargeCollected(state.getTotalSurchargeCollected().add(surcharge));

        state.setTotalCollections(state.getTotalCollections().add(totalPayment));

        updateDerivedValues(state);
        repository.save(state);
        eventPublisher.publishEvent(new FinancialStateUpdatedEvent(state));
    }

    @Transactional
    public void applyExpense(BigDecimal amount) {
        SystemFinancialState state = getCurrentState();
        state.setCashOnHand(state.getCashOnHand().subtract(amount));
        state.setTotalExpenses(state.getTotalExpenses().add(amount));

        updateDerivedValues(state);
        repository.save(state);
        eventPublisher.publishEvent(new FinancialStateUpdatedEvent(state));
    }

    @Transactional
    public void applySavingsTransaction(BigDecimal amount, boolean isDeposit) {
        SystemFinancialState state = getCurrentState();
        if (isDeposit) {
            state.setCashOnHand(state.getCashOnHand().add(amount));
            state.setCustomerSavings(state.getCustomerSavings().add(amount));
        } else {
            state.setCashOnHand(state.getCashOnHand().subtract(amount));
            state.setCustomerSavings(state.getCustomerSavings().subtract(amount));
        }

        updateDerivedValues(state);
        repository.save(state);
        eventPublisher.publishEvent(new FinancialStateUpdatedEvent(state));
    }

    @Transactional
    public void applyCapitalTransaction(BigDecimal amount, boolean isInjection) {
        SystemFinancialState state = getCurrentState();
        if (isInjection) {
            state.setCashOnHand(state.getCashOnHand().add(amount));
            state.setOwnerCapital(state.getOwnerCapital().add(amount));
        } else {
            state.setCashOnHand(state.getCashOnHand().subtract(amount));
            state.setOwnerCapital(state.getOwnerCapital().subtract(amount));
        }

        updateDerivedValues(state);
        repository.save(state);
        eventPublisher.publishEvent(new FinancialStateUpdatedEvent(state));
    }

    @Transactional
    public void applyBankTransaction(BigDecimal amount, boolean isDeposit) {
        SystemFinancialState state = getCurrentState();
        if (isDeposit) {
            state.setCashOnHand(state.getCashOnHand().subtract(amount));
            state.setBankBalance(state.getBankBalance().add(amount));
        } else {
            state.setCashOnHand(state.getCashOnHand().add(amount));
            state.setBankBalance(state.getBankBalance().subtract(amount));
        }

        updateDerivedValues(state);
        repository.save(state);
        eventPublisher.publishEvent(new FinancialStateUpdatedEvent(state));
    }

    private void updateDerivedValues(SystemFinancialState state) {
        // netProfit = interestReceived + processing fees + surcharge - totalExpenses
        BigDecimal income = state.getInterestReceived()
                .add(state.getTotalFeesCollected())
                .add(state.getTotalSurchargeCollected());
        state.setNetProfit(income.subtract(state.getTotalExpenses()));

        // grossLiquidity = cashOnHand + bankBalance
        state.setGrossLiquidity(state.getCashOnHand().add(state.getBankBalance()));

        // availableLiquidity = grossLiquidity - customerSavings (Simplest rule for MVP)
        state.setAvailableLiquidity(state.getGrossLiquidity().subtract(state.getCustomerSavings()));

        // expectedCash = amount of physical cash that should exist
        // This is basically cashOnHand in this model
        state.setExpectedCash(state.getCashOnHand());

        // cashVariance = actualCash - expectedCash
        state.setCashVariance(state.getActualCash().subtract(state.getExpectedCash()));
    }

    public BigDecimal getAvailableLiquidity(){
        SystemFinancialState state = getCurrentState();
        return state.getAvailableLiquidity();
    }

    public SystemFinancialState getCurrentState(){
        return repository.findFirstById(1L).orElseGet(SystemFinancialState::new);
    }

    @Transactional
    public void initialiseSystemState(SystemFinancialState state){
        updateDerivedValues(state);
        repository.save(state);
        eventPublisher.publishEvent(new FinancialStateUpdatedEvent(state));
    }

    @Transactional
    public void makeFinancialSetting(BigDecimal bankBalance, BigDecimal ownersCapital, BigDecimal cashOnHand, String adminEmails) {
        SystemFinancialState state = getCurrentState();
        state.setBankBalance(bankBalance);
        state.setOwnerCapital(ownersCapital);
        state.setCashOnHand(cashOnHand);
        state.setAdminEmails(adminEmails);
        updateDerivedValues(state);
        repository.save(state);
        eventPublisher.publishEvent(new FinancialStateUpdatedEvent(state));
    }
}
