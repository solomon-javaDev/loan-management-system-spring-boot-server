package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.AuditLog;
import io.sol.loanmanagementsystemspringbootserver.repositories.*;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final UserSession userSession;
    private final CustomerRepository customerRepository;
    private final LoansRepository loansRepository;
    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;
    private final ExpenseRepository expenseRepository;
    private final CashTransactionRepository cashTransactionRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final FeeBucketRepository feeBucketRepository;

    @Transactional
    public void record(String action, String entityType, Object entityId, String details) {
        auditLogRepository.save(new AuditLog(userSession.getCurrentUser(), action, entityType,
                String.valueOf(entityId), details));
    }

    public Result<List<AuditLog>> getAll() {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) {
            return Result.forbidden(authorization.message(), null);
        }
        return Result.success("Audit logs loaded", auditLogRepository.findAllByOrderByOccurredAtDesc());
    }

    public Result<List<AuditLog>> getDeletedRecords() {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) {
            return Result.forbidden(authorization.message(), null);
        }
        return Result.success("Deleted records loaded", auditLogRepository
                .findByUsernameContainingIgnoreCaseAndActionContainingIgnoreCaseAndEntityTypeContainingIgnoreCaseAndOccurredAtBetweenOrderByOccurredAtDesc(
                        "", "SOFT_DELETE", "", LocalDateTime.MIN, LocalDateTime.MAX));
    }

    public Result<List<AuditLog>> search(String username, String action, String entityType, LocalDate date) {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) {
            return Result.forbidden(authorization.message(), null);
        }
        LocalDate selectedDate = date == null ? LocalDate.now() : date;
        List<AuditLog> logs = auditLogRepository
                .findByUsernameContainingIgnoreCaseAndActionContainingIgnoreCaseAndEntityTypeContainingIgnoreCaseAndOccurredAtBetweenOrderByOccurredAtDesc(
                        value(username), value(action), value(entityType), selectedDate.atStartOfDay(), selectedDate.atTime(LocalTime.MAX));
        return Result.success("Audit logs filtered", logs);
    }

    @Transactional
    public Result<Void> restore(String entityType, Long id) {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) {
            return Result.forbidden(authorization.message(), null);
        }
        int restored = switch (entityType.toUpperCase()) {
            case "CUSTOMER" -> customerRepository.restoreById(id.intValue());
            case "LOAN" -> loansRepository.restoreById(id.intValue());
            case "PAYMENT" -> paymentRepository.restoreById(id.intValue());
            case "EMPLOYEE" -> employeeRepository.restoreById(id.intValue());
            case "EXPENSE" -> expenseRepository.restoreById(id);
            case "CASH_TRANSACTION" -> cashTransactionRepository.restoreById(id);
            case "EXPENSE_CATEGORY" -> expenseCategoryRepository.restoreById(id);
            case "FEE_BUCKET" -> feeBucketRepository.restoreById(id);
            default -> 0;
        };
        if (restored == 0) {
            return Result.notFound("Deleted record was not found", null);
        }
        record("RESTORE", entityType, id, "Record restored by administrator");
        return Result.success("Record restored successfully", null);
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }
}
