package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.FeeBucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface FeeBucketRepository extends JpaRepository<FeeBucket, Long> {
    List<FeeBucket> findAllByOrderByMinAmountAsc();

    @Query(value = "SELECT * FROM fee_buckets WHERE deleted = true", nativeQuery = true)
    List<FeeBucket> findAllDeleted();

    @Query(value = "SELECT * FROM fee_buckets WHERE id = :id", nativeQuery = true)
    Optional<FeeBucket> findByIncludingDeleted(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE fee_buckets SET deleted = false WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Long id);
}
