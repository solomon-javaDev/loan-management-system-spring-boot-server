package io.sol.loanmanagementsystemspringbootserver.config;

import io.sol.loanmanagementsystemspringbootserver.entities.*;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.EmployeeRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Configuration
public class SeedDataConfig {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder, CustomerRepository customerRepository, LoansRepository loansRepository, EmployeeRepository employeeRepository){
            return args ->  {
                if(userRepository.count() == 0){
                    User adminUser = new User();
                    adminUser.setEmail("admin@lms.com");
                    adminUser.setUsername("admin");
                    adminUser.setRole(Role.ADMIN);
                    adminUser.setPassword(passwordEncoder.encode("admin123"));
                    userRepository.save(adminUser);

                                    }
                if(customerRepository.count() == 0){
                    List<Customer> customers = List.of(
                            new Customer("Anita","Nannungi", " ", "nannungi@gmail.com", "0778909762", "ID001", "Kampala"),
                            new Customer("Nigel","Ukasha", "Jo", "fjk671gi@gmail.com", "0778908762", "ID002", "Entebbe"),
                            new Customer("Indl","Lindl", " ", "lin90@gmail.com", "0778908762", "ID003", "Jinja"),
                            new Customer("Aws","Sseki", "Emma ", "EmmanuelSeed9@gmail.com", "0768908762", "ID004", "Mbarara")
                    );

                    customerRepository.saveAll(customers);
                    System.out.println("Saved a List of customers "+customerRepository.count());
                }else{
                    System.out.println("All is well, customers are present");
                }

                if(employeeRepository.count() <= 2){

                    Employee employee = new Employee();
                    employee.setFirstName("Anita");
                    employee.setLastName("Nannungi Nigel");
                    employee.setUsername(employee.getFirstName()+" "+employee.getLastName());
                    employee.setPassword("0000");
                    employee.setRole(Role.FIELD_OFFICER);
                    employee.setSalary(10000);

                    Employee employee1 = new Employee();
                    employee1.setFirstName("Solomon");
                    employee1.setLastName("Kalungi");
                    employee1.setUsername(employee1.getFirstName()+" "+employee1.getLastName());
                    employee1.setPassword("0000");
                    employee1.setRole(Role.FIELD_OFFICER);
                    employee1.setSalary(13000);

                    Employee employee2 = new Employee();
                    employee2.setFirstName("Randiloph");
                    employee2.setLastName("Randy");
                    employee2.setUsername(employee2.getFirstName()+" "+employee2.getLastName());
                    employee2.setPassword("0000");
                    employee2.setRole(Role.FIELD_OFFICER);
                    employee2.setSalary(15000);

                    employeeRepository.save(employee1);
                    employeeRepository.save(employee2);
                    employeeRepository.save(employee);

                }
                if(loansRepository.count() == 0){

                    Loan loan = new Loan();
                    loan.setStartDate(LocalDate.now());
                    loan.setMaturityDate(LocalDate.now().plusMonths(6));
                    loan.setPrincipal(new BigDecimal("1000.00"));
                    loan.setStatus(LoanStatus.PENDING);
                    loan.setCustomer(customerRepository.findByFirstName("Nigel"));
                    loan.setFieldOfficer(employeeRepository.findByFirstName("Anita"));
                    loan.setInterestRate(BigDecimal.valueOf(0.01));
                    loan.setFees(BigDecimal.valueOf(3));
                    loan.setCollateral("Phone, Motorola Edge 24");
                    loan.setTenor(4);

                    Loan loan1 = new Loan(LocalDate.of(2026, 7, 23),LocalDate.of(2026, 7, 23), LocalDate.of(2026, 7, 23), BigDecimal.valueOf(200000), BigDecimal.valueOf(2), 2, "Phone and PC", BigDecimal.valueOf(12)  );
                    loan1.setStatus(LoanStatus.PENDING);
                    loansRepository.save(loan);
                    loansRepository.save(loan1);

                    System.out.println("Saved a List of loans "+loansRepository.count());
                }
        };
    }
}
