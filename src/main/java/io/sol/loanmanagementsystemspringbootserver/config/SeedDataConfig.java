package io.sol.loanmanagementsystemspringbootserver.config;

import io.sol.loanmanagementsystemspringbootserver.entities.User;
import io.sol.loanmanagementsystemspringbootserver.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedDataConfig {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder){
            return args ->  {
                if(userRepository.count() == 0){
                    User adminUser = new User();
                    adminUser.setEmail("admin@lms.com");
                    adminUser.setUsername("admin");
                    adminUser.setPassword(passwordEncoder.encode("admin123"));
                    userRepository.save(adminUser);

                                    }
        };
    }
}
