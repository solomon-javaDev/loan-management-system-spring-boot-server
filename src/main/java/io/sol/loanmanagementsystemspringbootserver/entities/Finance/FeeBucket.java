package io.sol.loanmanagementsystemspringbootserver.entities.Finance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "fee_buckets")
@Getter @Setter @NoArgsConstructor
public class FeeBucket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
