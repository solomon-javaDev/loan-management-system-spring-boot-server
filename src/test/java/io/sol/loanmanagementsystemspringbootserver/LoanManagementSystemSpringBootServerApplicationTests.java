package io.sol.loanmanagementsystemspringbootserver;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Payment;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:lms_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
class LoanManagementSystemSpringBootServerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void legacyPaymentsRemainVisibleToLoanCalculations() {
        assertEquals("(deleted = false OR deleted IS NULL)",
                Payment.class.getAnnotation(SQLRestriction.class).value());
    }

}
