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
}
