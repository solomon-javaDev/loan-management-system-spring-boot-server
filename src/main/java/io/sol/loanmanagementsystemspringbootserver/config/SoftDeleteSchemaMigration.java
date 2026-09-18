package io.sol.loanmanagementsystemspringbootserver.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Adds soft-delete columns to databases created before soft deletes were introduced. */
@Component
@Order(1)
public class SoftDeleteSchemaMigration implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    public SoftDeleteSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        addDeletedColumn("customer");
        addDeletedColumn("employees");
        addDeletedColumn("loans");
        addDeletedColumn("payments");
        addDeletedColumn("expenses");
        addDeletedColumn("cash_transactions");
        addDeletedColumn("expense_categories");
        addDeletedColumn("fee_buckets");
    }

    private void addDeletedColumn(String tableName) {
        jdbcTemplate.execute("ALTER TABLE " + tableName
                + " ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE NOT NULL");
    }
}
