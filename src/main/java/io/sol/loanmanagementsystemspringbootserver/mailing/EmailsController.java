package io.sol.loanmanagementsystemspringbootserver.mailing;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailsController {


    private final EmailsService emailsService;

    public EmailsController(EmailsService emailsService) {
        this.emailsService = emailsService;
    }

    @PostMapping("/sendEmail")
    public String sendEmail(@RequestBody EmailDetails details) {
        return String.valueOf(emailsService.sendSimpleMail(details));
    }

    @PostMapping("/sendEmailWithAttachment")
    public String sendMailWithAttachment(@RequestBody EmailDetails details) {
        return emailsService.sendMailWithAttachment(details);
    }
}