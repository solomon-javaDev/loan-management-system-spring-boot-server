package io.sol.loanmanagementsystemspringbootserver.config;

import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.entities.User;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class SeedDataConfig {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder, CustomerRepository customerRepository){
            return args ->  {
                if(userRepository.count() == 0){
                    User adminUser = new User();
                    adminUser.setEmail("admin@lms.com");
                    adminUser.setUsername("admin");
                    adminUser.setRole(Role.ADMIN);
                    adminUser.setPassword(passwordEncoder.encode("admin123"));
                    userRepository.save(adminUser);

                                    }
                if(customerRepository.count() <= 10){
                    List<Customer> customers = List.of(
                            new Customer("Anita","Nannungi", " ", "nannungi@gmail.com", "0778909762"),
                            new Customer("Nigel","Ukasha", "Jo", "fjk671gi@gmail.com", "0778908762"),
                            new Customer("Indl","Lindl", " ", "lin90@gmail.com", "0778908762"),
                            new Customer("Aws","Sseki", "Emma ", "EmmanuelSeed9@gmail.com", "0768908762"),
                            new Customer("Solo","Twist", "Mwesigwa", "mwesigwa78@gmail.com", "0778938762"),
                            new Customer("Adrof","Tweheyo", " ", "AdrofT@gmail.com", "0778908562"),
                            new Customer("Profit","Wahweza", "Ochom", "wahwezwa@gmail.com", "0778108762"),
                            new Customer("Enger","Obol", "James", "JamesEnger@gmail.com", "0778902662"),
                            new Customer("Seed","Levels", "Wampiki", "WampikiSS@gmail.com", "0778900262"),
                            new Customer("Adam","Eyasooka", "Kitabi", "Eyasooka@gmail.com", "0778905962"),
                            new Customer("Obungi","Ocholom", "Ekintu", "ekintuOO@gmail.com", "0778903762"),
                            new Customer("Obina","Kendel", "Justin ", "Justin@gmail.com", "0778295762"),
                            new Customer("Lamu","Olweyo", "Kiki kyo", "Kiki@gmail.com", "0778900382"),
                            new Customer("Ayam","Ayam", "White", "whiteyam@gmail.com", "0778498762"),
                            new Customer("Thor","Rocky", "Rookie ", "rookies@gmail.com", "0778908902"),
                            new Customer("Grants","Grant", "Atwine", "AtwineGrant@gmail.com", "0778208762"),
                            new Customer("Ibiza","ichraft", " ", "Ibiza@gmail.com", "0778945762"),
                            new Customer("Jave","Ssempala", " ", "Ssemplaz@gmail.com", "07782908762"),
                            new Customer("Finished","Yam", "Kikompola", "yamKikompola@gmail.com", "0778988762")
                    );

                    customerRepository.saveAll(customers);
                    System.out.println("Saved a List of customers "+customerRepository.count());
                }else{
                    System.out.println("All is well, customers are present");
                }
        };
    }
}
