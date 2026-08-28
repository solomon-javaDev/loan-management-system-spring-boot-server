package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.ExpenseCategory;
import io.sol.loanmanagementsystemspringbootserver.repositories.ExpenseCategoryRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository repository;

    public ExpenseCategoryService(ExpenseCategoryRepository repository){
        this.repository = repository;

    }

    public Result<List<ExpenseCategory>> getAllCategories(){
        return
                Result.success("Ok",
                repository.findAll());
    }

    public Result<ExpenseCategory> saveCategory(ExpenseCategory category){
     return   Result.success("Success",repository.save(category));
    }

    public Result deleteCategory(ExpenseCategory category){
        if(repository.existsByDescription(category.getDescription())){
            repository.delete(category);
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
}
