package io.sol.loanmanagementsystemspringbootserver;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@SpringBootApplication
public class LoanManagementSystemSpringBootServerApplication {
    public static void main(String[] args) {
        FxMainApp.launch(FxMainApp.class, args);
    }


    @Bean
    @Primary // Overrides any other detected configuration sources automatically
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://localhost:5432/loan_management_db")
                .username("postgres")
                .password("milka")
                .build();
    }
}
