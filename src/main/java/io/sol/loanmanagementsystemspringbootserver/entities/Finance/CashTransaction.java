package io.sol.loanmanagementsystemspringbootserver.entities.Finance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import io.sol.loanmanagementsystemspringbootserver.entities.custom.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cash_transactions")
@SQLDelete(sql = "UPDATE cash_transactions SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter @Setter @NoArgsConstructor
public class CashTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean deleted = false;

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
