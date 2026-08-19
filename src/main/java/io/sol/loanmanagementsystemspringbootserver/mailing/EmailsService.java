package io.sol.loanmanagementsystemspringbootserver.mailing;

/**
 * Service class that defines methods required for sending emails
 *
 */

public interface EmailsService {

    String sendSimpleMail(EmailDetails details);

    String sendMailWithAttachment(EmailDetails details);

}