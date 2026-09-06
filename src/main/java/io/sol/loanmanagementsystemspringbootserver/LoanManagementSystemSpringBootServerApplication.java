package io.sol.loanmanagementsystemspringbootserver;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoanManagementSystemSpringBootServerApplication {
    public static void main(String[] args) {
        FxMainApp.launch(FxMainApp.class, args);
    }
}
