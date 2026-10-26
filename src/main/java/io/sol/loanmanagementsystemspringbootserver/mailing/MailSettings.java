package io.sol.loanmanagementsystemspringbootserver.mailing;

public record MailSettings(String host, int port, String username, boolean startTls, boolean ssl) {
}