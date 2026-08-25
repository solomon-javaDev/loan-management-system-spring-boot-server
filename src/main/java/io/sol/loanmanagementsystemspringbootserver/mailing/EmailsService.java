package io.sol.loanmanagementsystemspringbootserver.mailing;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Service class that defines methods required for sending emails
 *
 */

@Component
public interface EmailsService {

    String sendSimpleMail(EmailDetails details);

    String sendMailWithAttachment(EmailDetails details);

}