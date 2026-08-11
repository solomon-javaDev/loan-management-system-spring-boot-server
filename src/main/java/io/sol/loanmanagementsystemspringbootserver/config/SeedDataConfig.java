package io.sol.loanmanagementsystemspringbootserver.config;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerCreateDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.*;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.EmployeeRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.UserRepository;
import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;

@Configuration
public class SeedDataConfig {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder, CustomerRepository customerRepository, LoansRepository loansRepository, EmployeeRepository employeeRepository, CustomerService customerService){
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
                            new Customer("Anita","Nannungi", " ", "nannungi@gmail.com", "0778909762",  "Kampala"),
                            new Customer("Nigel","Ukasha", "Jo", "fjk671gi@gmail.com", "0778908762",  "Entebbe"),
                            new Customer("Indl","Lindl", " ", "lin90@gmail.com", "0778908762","Jinja"),
                            new Customer("Aws","Sseki", "Emma ", "EmmanuelSeed9@gmail.com", "0768908762", "Mbarara")
                    );

                    customers.forEach(customer -> {
                        customer.setDeleted(FALSE);
                        customer.setAccountNumber(customerService.generateAccountNumber());

                        customerService.createCustomer(DTOMapper.toCreateDTO(customer));
                                            });


                    System.out.println("Saved a List of customers "+customerRepository.count());
                }else{
                    System.out.println("All is well, customers are present");
                }

                if(employeeRepository.count() <= 2){


                    List<Employee> field_officers = new ArrayList<>();
                    field_officers.add(new Employee("Solomon", "Twist", 20000, "solomon@twist.com", Role.FIELD_OFFICER));
                    field_officers.add(new Employee("Nisha", "Twist", 20300, "nisha@twist.com", Role.FIELD_OFFICER));
                    field_officers.add(new Employee("Hansa", "Gans", 20000, "hans@gans.com", Role.FIELD_OFFICER));
                    field_officers.add(new Employee("Hasifa", "Muus", 12000, "has@has.com", Role.FIELD_OFFICER));
                    field_officers.add(new Employee("Wendy", "Glav", 20000, "wens@wen.com", Role.FIELD_OFFICER));

                    employeeRepository.saveAll(field_officers);

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
