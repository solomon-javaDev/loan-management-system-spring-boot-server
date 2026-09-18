package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByOccurredAtDesc();

    List<AuditLog> findByUsernameContainingIgnoreCaseAndActionContainingIgnoreCaseAndEntityTypeContainingIgnoreCaseAndOccurredAtBetweenOrderByOccurredAtDesc(
            String username, String action, String entityType, LocalDateTime start, LocalDateTime end);
}
