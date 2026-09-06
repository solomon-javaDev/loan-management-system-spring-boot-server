package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.ExpenseCategory;
import io.sol.loanmanagementsystemspringbootserver.repositories.ExpenseCategoryRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UserSession;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository repository;
    private final UserSession userSession;
    private final AuditService auditService;

    public ExpenseCategoryService(ExpenseCategoryRepository repository, UserSession userSession, AuditService auditService){
        this.repository = repository;
        this.userSession = userSession;
        this.auditService = auditService;

    }

    public Result<List<ExpenseCategory>> getAllCategories(){
        return
                Result.success("Ok",
                repository.findAll());
    }

    public Result<ExpenseCategory> saveCategory(ExpenseCategory category){
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
     return   Result.success("Success",repository.save(category));
    }

    public Result deleteCategory(ExpenseCategory category){
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        if(repository.existsByDescription(category.getDescription())){
            repository.delete(category);
            auditService.record("SOFT_DELETE", "EXPENSE_CATEGORY", category.getId(), "Expense category soft deleted");
        }
        return Result.success("Deleted", null);
    }

    public Result<ExpenseCategory> getCategoryByDescription(String description) {
        if (description == null || description.isBlank()) {
            return Result.notFound("Category description is required", null);
        }

        ExpenseCategory result = repository.findByDescription(description.trim());

        if (result == null) {
            return Result.notFound("Category not found", null);
        }

        return Result.success("Ok", result);
    }

    public Result<List<ExpenseCategory>> getDeletedCategories() {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        return Result.success("Deleted categories loaded", repository.findAllDeleted());
    }
}
