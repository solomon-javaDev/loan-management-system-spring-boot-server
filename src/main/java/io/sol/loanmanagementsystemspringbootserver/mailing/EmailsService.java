package io.sol.loanmanagementsystemspringbootserver.mailing;

import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import org.springframework.stereotype.Component;

/**
 * Service class that defines methods required for sending emails
 *
 */

@Component
public interface EmailsService {

    Result<Object> sendSimpleMail(EmailDetails details);

    String sendMailWithAttachment(EmailDetails details);

}