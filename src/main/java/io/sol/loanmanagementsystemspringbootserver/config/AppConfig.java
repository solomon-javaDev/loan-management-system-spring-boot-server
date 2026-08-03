package io.sol.loanmanagementsystemspringbootserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * AppConfig class serves as a configuration class for defining beans needed within the application.
 * This includes the configuration for a {@link PasswordEncoder} bean to handle password encoding.
 *
 * Annotated with {@code @Configuration} to denote that this class contains bean definitions that
 * are managed by the Spring container.
 */

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
