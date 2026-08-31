package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.Customer;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.Employee;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailDetails;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailsService;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.LoanStatus;

@Service
public class DailyNotificationsService implements CommandLineRunner {
    private final CustomerRepository repository;
    private final EmployeeRepository employeeRepository;
    private final EmailsService emailsService;
    private final ReportService reportService;
    private final FinancialStateService financialStateService;

    public DailyNotificationsService(CustomerRepository repository, EmployeeRepository employeeRepository, 
                                     EmailsService emailsService, ReportService reportService, 
                                     FinancialStateService financialStateService) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.emailsService = emailsService;
        this.reportService = reportService;
        this.financialStateService = financialStateService;
    }

    @Override
    public void run(String... args) {
        // Catch-up: if the PC was off at 6 AM, send the missed officer emails on app open.
        // Guarded so a mail/network failure can't abort application startup.
        try {
            sendMorningCollection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 6 * *  ?")
    public void sendMorningCollection(){
        LocalDate today = LocalDate.now();

        // 1. Send reports to Admin
        io.sol.loanmanagementsystemspringbootserver.entities.Finance.SystemFinancialState state = financialStateService.getCurrentState();
        if (state.getAdminEmails() != null && !state.getAdminEmails().isBlank()) {
            byte[] dailyReport = reportService.generateDailyReport(today.minusDays(1)); // yesterday's report
            byte[] agingAnalysis = reportService.generateAgingAnalysis();

            for (String adminEmail : state.getAdminEmails().split(",")) {
                EmailDetails details = new EmailDetails();
                details.setRecipient(adminEmail.trim());
                details.setSubject("Daily System Reports - " + today);
                details.setBody("Please find attached the daily report and aging analysis.");
                
                details.setAttachment(dailyReport);
                details.setAttachmentName("DailyReport_" + today.minusDays(1) + ".pdf");
                emailsService.sendMailWithAttachment(details);

                details.setAttachment(agingAnalysis);
                details.setAttachmentName("AgingAnalysis_" + today + ".pdf");
                emailsService.sendMailWithAttachment(details);
            }
        }

        // 2. Fetch the field officers directly
        List<Employee> fieldOfficers = employeeRepository.findByRole(Role.FIELD_OFFICER);

        // 3. Loop through the employee objects directly to keep email and username linked
        for (Employee officer : fieldOfficers) {
            String username = officer.getUsername();
            String email = officer.getEmail();

            List<Customer> dueCustomers = repository.findCustomersByDueForFieldOfficer(username, today);

            if (dueCustomers.isEmpty()) {
                continue;
            }

            dueCustomers.sort(Comparator.comparingInt(
                    (Customer c) -> c.getLoans().stream()
                            .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.DEFAULTED)
                            .mapToInt(l -> (int) l.getAgingDays(today))
                            .max().orElse(0)).reversed());

            sendMailToOfficer(email, dueCustomers);
        }
    }


    private void sendMailToOfficer(String fieldOfficerEmail, List<Customer> dueCustomers) {
        EmailDetails details = new EmailDetails();
        details.setRecipient(fieldOfficerEmail);
        details.setSubject("Daily Collections List - "+ LocalDate.now());

        StringBuilder body = new StringBuilder();
        body.append("Greetings,\n\n");
        body.append("Here is your collections list for today:\n");

        for(Customer c: dueCustomers){
            int aging = c.getLoans().stream()
                    .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.DEFAULTED)
                    .mapToInt(l -> (int) l.getAgingDays(LocalDate.now())).max().orElse(0);
            body.append("- ").append(c.getFirstName()).append(" ").append(c.getLastName())
                    .append(" (Tel: ").append(c.getTelephone()).append(") | Aging: ").append(aging).append(" days\n");
        }

        details.setBody(body.toString());
        emailsService.sendSimpleMail(details);
    }
}
