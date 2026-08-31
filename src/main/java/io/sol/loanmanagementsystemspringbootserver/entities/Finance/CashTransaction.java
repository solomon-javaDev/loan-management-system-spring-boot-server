package io.sol.loanmanagementsystemspringbootserver.entities.Finance;

import io.sol.loanmanagementsystemspringbootserver.entities.custom.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_transactions")
@Getter @Setter @NoArgsConstructor
public class CashTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashTransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime date;

    private String description;
    private String reference;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User recordedBy;
    
    // For savings transactions
    private Integer customerId;

    public CashTransaction(CashTransactionType type, BigDecimal amount, String description, User recordedBy) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.recordedBy = recordedBy;
        this.date = LocalDateTime.now();
    }
}
