package io.sol.loanmanagementsystemspringbootserver.mailing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDetails {
    private String recipient;
    private String subject;
    private String body;
    private byte[] attachment;
    private String attachmentName;

    public EmailDetails(String recipient, String subject, String body) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }
}

