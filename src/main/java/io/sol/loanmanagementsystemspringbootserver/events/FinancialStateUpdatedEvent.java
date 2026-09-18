package io.sol.loanmanagementsystemspringbootserver.events;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.SystemFinancialState;

public record FinancialStateUpdatedEvent(SystemFinancialState state) {
}
