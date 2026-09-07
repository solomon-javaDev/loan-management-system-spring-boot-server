package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.FeeBucket;
import io.sol.loanmanagementsystemspringbootserver.repositories.FeeBucketRepository;
import lombok.RequiredArgsConstructor;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UserSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeBucketService {

    private final FeeBucketRepository repository;
    private final UserSession userSession;
    private final AuditService auditService;

    public BigDecimal calculateFee(BigDecimal principal) {
        if (principal == null) return BigDecimal.ZERO;
        
        return repository.findAllByOrderByMinAmountAsc().stream()
                .filter(bucket -> principal.compareTo(bucket.getMinAmount()) >= 0 && principal.compareTo(bucket.getMaxAmount()) <= 0)
                .findFirst()
                .map(bucket -> bucket.getFeeAmount())
                .orElse(BigDecimal.ZERO);
    }

    public List<FeeBucket> getAllBuckets() {
        return repository.findAllByOrderByMinAmountAsc();
    }

    public Result<FeeBucket> saveBucket(FeeBucket bucket) {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        if (bucket == null || bucket.getMinAmount() == null || bucket.getMaxAmount() == null
                || bucket.getFeeAmount() == null) {
            return Result.invalid("Minimum, maximum, and fee amounts are required.", null);
        }
        if (bucket.getMinAmount().compareTo(BigDecimal.ZERO) < 0
                || bucket.getMaxAmount().compareTo(bucket.getMinAmount()) < 0
                || bucket.getFeeAmount().compareTo(BigDecimal.ZERO) < 0) {
            return Result.invalid("Fee bucket amounts must be valid and non-negative, with maximum at least minimum.", null);
        }
        boolean overlaps = repository.findAllByOrderByMinAmountAsc().stream()
                .filter(existing -> !existing.getId().equals(bucket.getId()))
                .anyMatch(existing -> bucket.getMinAmount().compareTo(existing.getMaxAmount()) <= 0
                        && bucket.getMaxAmount().compareTo(existing.getMinAmount()) >= 0);
        if (overlaps) {
            return Result.invalid("Fee bucket range overlaps an existing bucket.", null);
        }
        FeeBucket saved = repository.save(bucket);
        auditService.record(bucket.getId() == null ? "CREATE" : "UPDATE", "FEE_BUCKET", saved.getId(),
                "Fee bucket saved");
        return Result.success("Fee bucket saved successfully.", saved);
    }

    public void deleteBucket(Long id) {
        if (!userSession.isAdmin()) return;
        repository.deleteById(id);
        auditService.record("SOFT_DELETE", "FEE_BUCKET", id, "Fee bucket soft deleted");
    }

    public Result<List<FeeBucket>> getDeletedBuckets() {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        return Result.success("Deleted fee buckets loaded", repository.findAllDeleted());
    }
}
