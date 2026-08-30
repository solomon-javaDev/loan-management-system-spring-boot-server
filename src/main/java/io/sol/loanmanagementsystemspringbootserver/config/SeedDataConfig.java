package io.sol.loanmanagementsystemspringbootserver.config;

import io.sol.loanmanagementsystemspringbootserver.entities.Employee;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.entities.User;
import io.sol.loanmanagementsystemspringbootserver.repositories.EmployeeRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class SeedDataConfig implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public SeedDataConfig(EmployeeRepository employeeRepository,
                          PasswordEncoder passwordEncoder,
                          UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        userRepository.findByUsername("admin")
                .ifPresentOrElse(admin -> {
                    if (isPlainTextPassword(admin.getPassword())) {
                        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
                        userRepository.save(admin);
                    }
                }, () -> {
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setEmail("admin@lms.com");
                    admin.setRole(Role.ADMIN);
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    userRepository.save(admin);
                });

        List<Employee> seeds = List.of(
                make("Solomon", "Twist", 20000, "solomon@twist.com", "0789847372"),
                make("Nisha",   "Twist", 20300, "nisha@twist.com",   "085958332"),
                make("Hansa",   "Gans",  20000, "hans@gans.com",     "0758437722"),
                make("Hasifa",  "Muus",  12000, "has@has.com",       "089483722"),
                make("Wendy",   "Glav",  20000, "wens@wen.com",      "093728923")
        );
        for (Employee e : seeds) {
            employeeRepository.findByFirstName(e.getFirstName())
                    .ifPresentOrElse(existing -> {
                        if (isPlainTextPassword(existing.getPassword())) {
                            existing.setPassword(passwordEncoder.encode(existing.getPassword()));
                            employeeRepository.save(existing);
                        }
                    }, () -> {
                        employeeRepository.save(e);
                    });
        }
    }

    private Employee make(String first, String last, int salary, String email, String phone) {
        Employee e = new Employee();
        e.setUsername(first + " " + last);
        e.setFirstName(first);
        e.setLastName(last);
        e.setSalary(salary);
        e.setEmail(email);
        e.setTelephone(phone);
        e.setRole(Role.FIELD_OFFICER);
        e.setActive(true);
        e.setPassword("123");
        return e;
    }

    private boolean isPlainTextPassword(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return !(value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}