package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.FeeBucket;
import io.sol.loanmanagementsystemspringbootserver.repositories.FeeBucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeBucketService {

    private final FeeBucketRepository repository;

    public BigDecimal calculateFee(BigDecimal principal) {
        if (principal == null) return BigDecimal.ZERO;
        
        return repository.findAllByOrderByMinAmountAsc().stream()
                .filter(bucket -> principal.compareTo(bucket.getMinAmount()) >= 0 && principal.compareTo(bucket.getMaxAmount()) <= 0)
                .findFirst()
                .map(FeeBucket::getFeeAmount)
                .orElse(BigDecimal.ZERO);
    }

    public List<FeeBucket> getAllBuckets() {
        return repository.findAllByOrderByMinAmountAsc();
    }

    public void saveBucket(FeeBucket bucket) {
        repository.save(bucket);
    }

    public void deleteBucket(Long id) {
        repository.deleteById(id);
    }
}
