package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.CapitalAccount;
import io.sol.loanmanagementsystemspringbootserver.repositories.CapitalAccountsRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.CapitalSummaryView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CapitalViewService {

    private static final UUID SYSTEM_ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final CapitalAccountsRepository capitalAccountsRepository;

    public CapitalViewService(CapitalAccountsRepository capitalAccountsRepository) {
        this.capitalAccountsRepository = capitalAccountsRepository;
    }

    @Transactional(readOnly = true)
    public CapitalSummaryView getLiveSummary(){
        CapitalAccount account = capitalAccountsRepository.findAndLockAccountById(SYSTEM_ACCOUNT_ID);

        return new CapitalSummaryView(
                account.getCashAtHand(),
                account.getActivePortfolioPrincipal(),
                account.getAccumulatedInterestEarned(),
                account.getWorkingCash()
        );
    }
}
