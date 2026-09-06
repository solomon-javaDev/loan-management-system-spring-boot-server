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
                .map(FeeBucket::getFeeAmount)
                .orElse(BigDecimal.ZERO);
    }

    public List<FeeBucket> getAllBuckets() {
        return repository.findAllByOrderByMinAmountAsc();
    }

    public void saveBucket(FeeBucket bucket) {
        if (!userSession.isAdmin()) return;
        repository.save(bucket);
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
