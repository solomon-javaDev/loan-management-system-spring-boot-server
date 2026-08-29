package io.sol.loanmanagementsystemspringbootserver;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@SpringBootApplication
@EnableScheduling
public class LoanManagementSystemSpringBootServerApplication {
    public static void main(String[] args) {
        FxMainApp.launch(FxMainApp.class, args);
    }


    @Bean
    @Primary // Overrides any other detected configuration sources automatically
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:file:~/loan_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
                .username("sa")
                .password("milka")
                .build();
    }

}
