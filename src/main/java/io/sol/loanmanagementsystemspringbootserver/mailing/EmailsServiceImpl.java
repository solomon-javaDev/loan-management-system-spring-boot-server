package io.sol.loanmanagementsystemspringbootserver.mailing;

import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailsServiceImpl implements EmailsService {

    private final JavaMailSender javaMailSender;
    private final EmailSettingsService emailSettingsService;

    @Value("${spring.mail.username:}")
    private String sender;

    public EmailsServiceImpl(JavaMailSender javaMailSender, EmailSettingsService emailSettingsService) {
        this.javaMailSender = javaMailSender;
        this.emailSettingsService = emailSettingsService;
    }

    @Override
    public Result<Object> sendSimpleMail(EmailDetails details) {
        try {
            JavaMailSender activeSender = emailSettingsService.hasSessionCredentials()
                    ? emailSettingsService.getConfiguredSender() : javaMailSender;
            String from = emailSettingsService.hasSessionCredentials()
                    ? emailSettingsService.getSettings().username() : sender;
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom(from);
            mailMessage.setTo(details.getRecipient());
            mailMessage.setText(details.getBody());
            mailMessage.setSubject(details.getSubject());

            activeSender.send(mailMessage);
            return Result.success("Mail Sent Successfully...", null);
        } catch (Exception e) {
            return Result.notFound("Error while Sending Mail", "Check SMTP configuration and network connection.");
        }
    }

    @Override
    public String sendMailWithAttachment(EmailDetails details) {
        try {
            JavaMailSender activeSender = emailSettingsService.hasSessionCredentials()
                    ? emailSettingsService.getConfiguredSender() : javaMailSender;
            String from = emailSettingsService.hasSessionCredentials()
                    ? emailSettingsService.getSettings().username() : sender;
            MimeMessage mimeMessage = activeSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper =
                new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getBody());
            mimeMessageHelper.setSubject(details.getSubject());

            if (details.getAttachment() != null) {
                mimeMessageHelper.addAttachment(details.getAttachmentName(), new org.springframework.core.io.ByteArrayResource(details.getAttachment()));
            }

            activeSender.send(mimeMessage);
            return "Mail Sent Successfully...";
        } catch (Exception e) {
            return "Error while Sending Mail: Check SMTP configuration and network connection.";
        }
    }
}
