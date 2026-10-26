package io.sol.loanmanagementsystemspringbootserver.mailing;

import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

@Service
public class EmailSettingsService {

    private final Preferences preferences;
    private final WindowsMailCredentialStore credentialStore;
    private volatile MailSettings currentSettings;
    private volatile String currentPassword = "";

    public EmailSettingsService() {
        this(Preferences.userRoot().node("io/sol/loanmanagementsystem/email"));
    }

    EmailSettingsService(Preferences preferences) {
        this.preferences = preferences;
        this.credentialStore = new WindowsMailCredentialStore(preferences);
        currentSettings = new MailSettings(
                preferences.get("host", "smtp.gmail.com"),
                preferences.getInt("port", 587),
                preferences.get("username", ""),
                preferences.getBoolean("startTls", true),
                preferences.getBoolean("ssl", false));
        currentPassword = credentialStore.load();
    }

    public MailSettings getSettings() {
        return currentSettings;
    }

    public boolean hasSessionCredentials() {
        return !currentPassword.isBlank();
    }

    public void testConnection(MailSettings settings, String password) throws MessagingException {
        validate(settings, password);
        createSender(settings, password).testConnection();
    }

    public synchronized void save(MailSettings settings, String password) throws BackingStoreException {
        validate(settings, password);
        credentialStore.save(password);
        preferences.put("host", settings.host());
        preferences.putInt("port", settings.port());
        preferences.put("username", settings.username());
        preferences.putBoolean("startTls", settings.startTls());
        preferences.putBoolean("ssl", settings.ssl());
        preferences.flush();
        currentSettings = settings;
        currentPassword = password;
    }

    public JavaMailSenderImpl getConfiguredSender() {
        String password = currentPassword;
        if (password.isBlank()) {
            throw new IllegalStateException("SMTP password is required for this session.");
        }
        return createSender(currentSettings, password);
    }

    public String savedPasswordOr(String entered) {
        return entered == null || entered.isBlank() ? currentPassword : entered;
    }

    static void validate(MailSettings settings, String password) {
        Objects.requireNonNull(settings, "Settings are required.");
        if (settings.host() == null || settings.host().isBlank()) {
            throw new IllegalArgumentException("SMTP host is required.");
        }
        if (settings.port() < 1 || settings.port() > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
        }
        if (settings.username() == null || !settings.username().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (settings.startTls() == settings.ssl()) {
            throw new IllegalArgumentException("Select STARTTLS or SSL/TLS.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("SMTP password or app password is required.");
        }
    }

    private JavaMailSenderImpl createSender(MailSettings settings, String password) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(settings.host());
        sender.setPort(settings.port());
        sender.setUsername(settings.username());
        sender.setPassword(password);
        sender.setProtocol("smtp");
        sender.getJavaMailProperties().put("mail.smtp.auth", "true");
        sender.getJavaMailProperties().put("mail.smtp.starttls.enable", Boolean.toString(settings.startTls()));
        sender.getJavaMailProperties().put("mail.smtp.starttls.required", Boolean.toString(settings.startTls()));
        sender.getJavaMailProperties().put("mail.smtp.ssl.enable", Boolean.toString(settings.ssl()));
        sender.getJavaMailProperties().put("mail.smtp.connectiontimeout", "10000");
        sender.getJavaMailProperties().put("mail.smtp.timeout", "10000");
        sender.getJavaMailProperties().put("mail.smtp.writetimeout", "10000");
        return sender;
    }
}