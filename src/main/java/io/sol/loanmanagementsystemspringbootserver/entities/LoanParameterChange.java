package io.sol.loanmanagementsystemspringbootserver.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_parameter_changes")
@Getter
@Setter
public class LoanParameterChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

    private String parameterName;
    private String oldValue;
    private String newValue;
    private String changedBy;
    private LocalDateTime changeTimestamp;

    public LoanParameterChange() {
        this.changeTimestamp = LocalDateTime.now();
    }

    public void setLoan(Loan loan) { this.loan = loan; }
    public Loan getLoan() { return loan; }
    public void setParameterName(String parameterName) { this.parameterName = parameterName; }
    public String getParameterName() { return parameterName; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getOldValue() { return oldValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getNewValue() { return newValue; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public String getChangedBy() { return changedBy; }
}
