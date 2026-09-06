package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    @Query(value = "SELECT * FROM expense_categories WHERE deleted = true", nativeQuery = true)
    List<ExpenseCategory> findAllDeleted();

    @Query(value = "SELECT * FROM expense_categories WHERE id = :id", nativeQuery = true)
    Optional<ExpenseCategory> findByIncludingDeleted(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE expense_categories SET deleted = false WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Long id);

    boolean existsByDescription(String description);

    public ExpenseCategory findByDescription(String description);
}
