package io.sol.loanmanagementsystemspringbootserver.mailing;

import com.sun.jna.platform.win32.Crypt32Util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
public class WindowsMailCredentialStore {
    private final Preferences preferences;

    WindowsMailCredentialStore(Preferences preferences) {
        this.preferences = preferences;
    }

    String load() {
        String encrypted = preferences.get("protectedPassword", "");
        if (encrypted.isEmpty()) return "";
        if (!isWindows()) return "";
        try {
            return new String(Crypt32Util.cryptUnprotectData(Base64.getDecoder().decode(encrypted)), StandardCharsets.UTF_8);
        } catch (RuntimeException ex) {
            return "";
        }
    }

    void save(String password) throws BackingStoreException {
        if (!isWindows()) throw new IllegalStateException("Secure SMTP password storage requires Windows.");
        byte[] encrypted = Crypt32Util.cryptProtectData(password.getBytes(StandardCharsets.UTF_8));
        preferences.put("protectedPassword", Base64.getEncoder().encodeToString(encrypted));
        preferences.flush();
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}