package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.CapitalAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CapitalAccountsRepository extends JpaRepository<CapitalAccount, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c from CapitalAccount c where c.id = :id")
    CapitalAccount findAndLockAccountById(@Param("id") UUID id);

    Optional<CapitalAccount> findById(UUID id);
}
