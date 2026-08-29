package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.entities.Employee;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.EmployeeRepository;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DailyNotificationsService {
    private final CustomerRepository repository;
    private final JavaMailSender javaMailSender;
    private final EmployeeRepository employeeRepository;
    private final MailSender mailSender;

    public DailyNotificationsService(CustomerRepository repository, JavaMailSender javaMailSender, EmployeeRepository employeeRepository, MailSender mailSender) {
        this.repository = repository;
        this.javaMailSender = javaMailSender;
        this.employeeRepository = employeeRepository;
        this.mailSender = mailSender;
    }

    @Scheduled(cron = "0 0 6 * *  ?")
    public void sendMorningCollection(){
        LocalDate today = LocalDate.now();

        // 1. Fetch the field officers directly
        List<Employee> fieldOfficers = employeeRepository.findByRole(Role.FIELD_OFFICER);

        // 2. Loop through the employee objects directly to keep email and username linked
        for (Employee officer : fieldOfficers) {
            String username = officer.getUsername();
            String email = officer.getEmail();

            List<Customer> dueCustomers = repository.findCustomersByDueForFieldOfficer(username, today);

            // 3. Using 'continue' instead of 'return' so we don't skip other officers
            if (dueCustomers.isEmpty()) {
                continue;
            }

            sendMailToOfficer(email, dueCustomers);
        }
    }


    private void sendMailToOfficer(String fieldOfficerEmail, List<Customer> dueCustomers) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(fieldOfficerEmail);
        message.setSubject("Daily Collections List - "+ LocalDate.now());

        StringBuilder body = new StringBuilder();
        body.append("Greetings, ").append(fieldOfficerEmail).append(",\n\n");
        body.append("Here is your collections list");

        for(Customer c: dueCustomers){
            body.append("- ").append(c.getFirstName()).append(" ").append(c.getLastName())
                    .append(" (Tel: ").append(c.getTelephone()).append(")\n");
        }

        message.setText(body.toString());
        mailSender.send(message);
    }
}
