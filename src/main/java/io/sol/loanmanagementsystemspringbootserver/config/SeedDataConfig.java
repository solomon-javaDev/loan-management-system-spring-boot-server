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
                            new Customer("Anita","Nannungi", "", "nannungi@gmail.com", "0778909762",  "Kampala"),
                            new Customer("Nigel","Ukasha", "Jo", "fjk671gi@gmail.com", "0778908762",  "Entebbe"),
                            new Customer("Indl","Lindl", "", "lin90@gmail.com", "0778908763","Jinja"),
                            new Customer("Aws","Sseki", "Emma", "EmmanuelSeed9@gmail.com", "0768908762", "Mbarara"),
                            new Customer("John","Doe", "", "john.doe@example.com", "0777000001", "Kampala"),
                            new Customer("Mary","Smith", "Ann", "mary.smith@example.com", "0777000002", "Entebbe"),
                            new Customer("Peter","Pan", "", "peter.pan@example.com", "0777000003", "Jinja"),
                            new Customer("Alice","Johnson", "", "alice.j@example.com", "0777000004", "Mbarara"),
                            new Customer("Bob","Brown", "", "bob.brown@example.com", "0777000005", "Kampala"),
                            new Customer("Charles","Kato", "", "charles.kato@example.com", "0777000006", "Gulu"),
                            new Customer("Diana","Nalu", "", "diana.nalu@example.com", "0777000007", "Mbale"),
                            new Customer("Eric","Omara", "", "eric.omara@example.com", "0777000008", "Fort Portal")
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

                    // Create a variety of loans to cover PENDING, ACTIVE, CLOSED, DEFAULTED scenarios
                    Employee sol = employeeRepository.findByFirstName("Solomon");
                    Employee nisha = employeeRepository.findByFirstName("Nisha");
                    Employee hansa = employeeRepository.findByFirstName("Hansa");
                    Employee hasifa = employeeRepository.findByFirstName("Hasifa");

                    Customer nigel = customerRepository.findByFirstName("Nigel");
                    Customer john = customerRepository.findByFirstName("John");
                    Customer mary = customerRepository.findByFirstName("Mary");
                    Customer peter = customerRepository.findByFirstName("Peter");
                    Customer alice = customerRepository.findByFirstName("Alice");
                    Customer bob = customerRepository.findByFirstName("Bob");

                    List<Loan> loans = new ArrayList<>();

                    // 1) Pending loan (no payments yet)
                    Loan l1 = new Loan();
                    l1.setStartDate(LocalDate.now().minusDays(2));
                    l1.setMaturityDate(LocalDate.now().plusMonths(6));
                    l1.setPrincipal(new BigDecimal("1000.00"));
                    l1.setInterestRate(new BigDecimal("0.05"));
                    l1.setFees(new BigDecimal("10"));
                    l1.setCollateral("Phone, Motorola Edge 24");
                    l1.setTenor(6);
                    l1.setStatus(LoanStatus.PENDING);
                    l1.setCustomer(nigel);
                    l1.setFieldOfficer(sol);
                    loans.add(l1);

                    // 2) Active loan with partial payments
                    Loan l2 = new Loan();
                    l2.setStartDate(LocalDate.now().minusMonths(3));
                    l2.setMaturityDate(LocalDate.now().plusMonths(3));
                    l2.setPrincipal(new BigDecimal("5000.00"));
                    l2.setInterestRate(new BigDecimal("0.10"));
                    l2.setFees(new BigDecimal("25"));
                    l2.setCollateral("Motorbike");
                    l2.setTenor(6);
                    l2.setStatus(LoanStatus.ACTIVE);
                    l2.setCustomer(john);
                    l2.setFieldOfficer(nisha);
                    Payment p21 = new Payment(); p21.setDate(LocalDate.now().minusMonths(2)); p21.setAmountReceived(new BigDecimal("1000")); l2.addPayment(p21);
                    Payment p22 = new Payment(); p22.setDate(LocalDate.now().minusMonths(1)); p22.setAmountReceived(new BigDecimal("500")); l2.addPayment(p22);
                    loans.add(l2);

                    // 3) Closed (fully paid)
                    Loan l3 = new Loan();
                    l3.setStartDate(LocalDate.of(2025, 1, 1));
                    l3.setMaturityDate(LocalDate.of(2025, 7, 1));
                    l3.setPrincipal(new BigDecimal("2000.00"));
                    l3.setInterestRate(new BigDecimal("0.10"));
                    l3.setFees(new BigDecimal("20"));
                    l3.setCollateral("Laptop");
                    l3.setTenor(6);
                    l3.setStatus(LoanStatus.CLOSED);
                    l3.setCustomer(mary);
                    l3.setFieldOfficer(hansa);
                    Payment p31 = new Payment(); p31.setDate(LocalDate.of(2025,2,1)); p31.setAmountReceived(new BigDecimal("1100")); l3.addPayment(p31);
                    Payment p32 = new Payment(); p32.setDate(LocalDate.of(2025,4,1)); p32.setAmountReceived(new BigDecimal("1200")); l3.addPayment(p32);
                    l3.setFullPaidDate(LocalDate.of(2025,4,1));
                    loans.add(l3);

                    // 4) Defaulted (partial payments but still outstanding)
                    Loan l4 = new Loan();
                    l4.setStartDate(LocalDate.now().minusMonths(12));
                    l4.setMaturityDate(LocalDate.now().minusMonths(6));
                    l4.setPrincipal(new BigDecimal("8000.00"));
                    l4.setInterestRate(new BigDecimal("0.15"));
                    l4.setFees(new BigDecimal("50"));
                    l4.setCollateral("Shop inventory");
                    l4.setTenor(12);
                    l4.setStatus(LoanStatus.DEFAULTED);
                    l4.setCustomer(peter);
                    l4.setFieldOfficer(hasifa);
                    Payment p41 = new Payment(); p41.setDate(LocalDate.now().minusMonths(10)); p41.setAmountReceived(new BigDecimal("1000")); l4.addPayment(p41);
                    loans.add(l4);

                    // 5) Active, multiple payments
                    Loan l5 = new Loan();
                    l5.setStartDate(LocalDate.now().minusMonths(2));
                    l5.setMaturityDate(LocalDate.now().plusMonths(10));
                    l5.setPrincipal(new BigDecimal("15000.00"));
                    l5.setInterestRate(new BigDecimal("0.08"));
                    l5.setFees(new BigDecimal("100"));
                    l5.setCollateral("Car");
                    l5.setTenor(12);
                    l5.setStatus(LoanStatus.ACTIVE);
                    l5.setCustomer(alice);
                    l5.setFieldOfficer(sol);
                    Payment p51 = new Payment(); p51.setDate(LocalDate.now().minusMonths(2)); p51.setAmountReceived(new BigDecimal("2000")); l5.addPayment(p51);
                    Payment p52 = new Payment(); p52.setDate(LocalDate.now().minusMonths(1)); p52.setAmountReceived(new BigDecimal("2500")); l5.addPayment(p52);
                    loans.add(l5);

                    // 6) Small pending loan
                    Loan l6 = new Loan();
                    l6.setStartDate(LocalDate.now());
                    l6.setMaturityDate(LocalDate.now().plusMonths(1));
                    l6.setPrincipal(new BigDecimal("300.00"));
                    l6.setInterestRate(new BigDecimal("0.02"));
                    l6.setFees(new BigDecimal("5"));
                    l6.setCollateral("Bicycle");
                    l6.setTenor(1);
                    l6.setStatus(LoanStatus.PENDING);
                    l6.setCustomer(bob);
                    l6.setFieldOfficer(nisha);
                    loans.add(l6);

                    // Save all loans (payments cascade)
                    loansRepository.saveAll(loans);

                    System.out.println("Saved a List of loans "+loansRepository.count());
                }
        };
    }
}
