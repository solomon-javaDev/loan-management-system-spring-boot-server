package io.sol.loanmanagementsystemspringbootserver.utilities;

public interface CustomerSummaryProjection {
    Long getId();
    String getFirstName();
    String getLastName();
    String getOtherNames();
    String getEmail();
    String getTelephone();
    Long getLoanCount();
}
