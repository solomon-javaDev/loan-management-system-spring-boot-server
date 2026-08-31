package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.FeeBucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeBucketRepository extends JpaRepository<FeeBucket, Long> {
    List<FeeBucket> findAllByOrderByMinAmountAsc();
}
