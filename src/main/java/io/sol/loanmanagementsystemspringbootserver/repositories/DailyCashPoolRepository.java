package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.DailyCashPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyCashPoolRepository extends JpaRepository<DailyCashPool, Long> {
    Optional<DailyCashPool> findByBusinessDate(LocalDate businessDate);
    Optional<DailyCashPool> findTopByBusinessDateBeforeOrderByBusinessDateDesc(LocalDate businessDate);
}
