package io.sol.loanmanagementsystemspringbootserver.entities.Finance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "fee_buckets")
@SQLDelete(sql = "UPDATE fee_buckets SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter @Setter @NoArgsConstructor
public class FeeBucket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private BigDecimal minAmount;

    @Column(nullable = false)
    private BigDecimal maxAmount;

    @Column(nullable = false)
    private BigDecimal feeAmount;

    public FeeBucket(BigDecimal minAmount, BigDecimal maxAmount, BigDecimal feeAmount) {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.feeAmount = feeAmount;
    }
}
