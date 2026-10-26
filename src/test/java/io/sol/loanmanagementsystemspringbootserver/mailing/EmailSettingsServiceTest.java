package io.sol.loanmanagementsystemspringbootserver.mailing;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

class EmailSettingsServiceTest {

    @Test
    void validatesSettingsAndDoesNotApplyInvalidValues() throws Exception {
        Preferences preferences = Preferences.userRoot().node("io/sol/loanmanagementsystem/tests/" + UUID.randomUUID());
        try {
            EmailSettingsService service = new EmailSettingsService(preferences);
            MailSettings initial = service.getSettings();
            assertEquals("smtp.gmail.com", initial.host());
            assertEquals(587, initial.port());
            assertThrows(IllegalArgumentException.class, () -> service.save(new MailSettings("", 587, "me@example.com", true, false), "secret"));
            assertThrows(IllegalArgumentException.class, () -> service.save(new MailSettings("host", 0, "me@example.com", true, false), "secret"));
            assertThrows(IllegalArgumentException.class, () -> service.save(new MailSettings("host", 587, "invalid", true, false), "secret"));
            assertThrows(IllegalArgumentException.class, () -> service.save(new MailSettings("host", 587, "me@example.com", true, true), "secret"));
            assertThrows(IllegalArgumentException.class, () -> service.save(new MailSettings("host", 587, "me@example.com", false, false), "secret"));
            assertThrows(IllegalArgumentException.class, () -> service.save(new MailSettings("host", 587, "me@example.com", true, false), ""));
            assertEquals(initial, service.getSettings());
        } finally {
            preferences.removeNode();
        }
    }

    @Test
    void savesProtectedPasswordAndRestoresSenderAfterRestart() throws Exception {
        Preferences preferences = Preferences.userRoot().node("io/sol/loanmanagementsystem/tests/" + UUID.randomUUID());
        try {
            EmailSettingsService service = new EmailSettingsService(preferences);
            MailSettings settings = new MailSettings("mail.example.com", 465, "me@example.com", false, true);
            service.save(settings, "secret-password");
            assertEquals(settings, service.getSettings());
            assertEquals("mail.example.com", service.getConfiguredSender().getHost());
            assertEquals("me@example.com", service.getConfiguredSender().getUsername());
            assertEquals("true", service.getConfiguredSender().getJavaMailProperties().get("mail.smtp.ssl.enable"));
            assertEquals(settings, new EmailSettingsService(preferences).getSettings());
            assertTrue(new EmailSettingsService(preferences).hasSessionCredentials());
            assertEquals("secret-password", new EmailSettingsService(preferences).getConfiguredSender().getPassword());
            assertFalse(preferences.keys().length == 0);
            for (String key : preferences.keys()) {
                assertNotEquals("secret-password", preferences.get(key, ""));
            }
        } finally {
            preferences.removeNode();
        }
    }
}