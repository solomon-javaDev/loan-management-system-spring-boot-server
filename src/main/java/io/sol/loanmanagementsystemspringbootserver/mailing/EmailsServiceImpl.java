package io.sol.loanmanagementsystemspringbootserver.mailing;

import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailsServiceImpl implements EmailsService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public EmailsServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public Result<Object> sendSimpleMail(EmailDetails details) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom(sender);
            mailMessage.setTo(details.getRecipient());
            mailMessage.setText(details.getBody());
            mailMessage.setSubject(details.getSubject());

            javaMailSender.send(mailMessage);
            return Result.success("Mail Sent Successfully...", null);
        } catch (Exception e) {
            return Result.notFound("Error while Sending Mail: ", e.getMessage());
        }
    }

    @Override
    public String sendMailWithAttachment(EmailDetails details) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper mimeMessageHelper = 
                new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getBody());
            mimeMessageHelper.setSubject(details.getSubject());

            if (details.getAttachment() != null) {
                mimeMessageHelper.addAttachment(details.getAttachmentName(), new org.springframework.core.io.ByteArrayResource(details.getAttachment()));
            }

            javaMailSender.send(mimeMessage);
            return "Mail Sent Successfully...";
        } catch (Exception e) {
            return "Error while Sending Mail: " + e.getMessage();
        }
    }
}
