package io.sol.loanmanagementsystemspringbootserver.licensing;

import io.sol.loanmanagementsystemspringbootserver.services.OfflineLicenseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LicenseIssuerTest {
    @TempDir Path state;

    @Test
    void activatesOfflineAndStacksRenewalsWithoutDoubleSpending() throws Exception {
        LicenseIssuer issuer = new LicenseIssuer(state);
        OfflineLicenseService client = new OfflineLicenseService();
        String installation = client.installationId();
        System.setProperty("lms.license.public-key", Files.readString(state.resolve("public.key")));
        try {
            Instant now = Instant.parse("2026-01-31T12:00:00Z");
            String code = issuer.issue();
            String first = issuer.activate(code, installation, now);
            Instant expiry = ZonedDateTime.ofInstant(now, ZoneOffset.UTC).plusMonths(3).toInstant();
            assertTrue(client.valid(first, expiry.minusSeconds(1)));
            assertFalse(client.valid(first, expiry));
            assertTrue(client.valid(issuer.activate(code, installation, now.plusSeconds(10)), expiry.minusSeconds(1)));
            assertThrows(IllegalArgumentException.class, () -> issuer.activate(code, "00000000-0000-0000-0000-000000000000", now));
            String renewal = issuer.activate(issuer.issue(), installation, now.plusSeconds(3600));
            assertTrue(client.valid(renewal, ZonedDateTime.ofInstant(expiry, ZoneOffset.UTC).plusMonths(3).toInstant().minusSeconds(1)));
            assertFalse(client.valid(renewal + "x", now));
            assertThrows(IllegalArgumentException.class, () -> issuer.activate("invalid", installation, now));
            assertTrue(new LicenseIssuer(state).activate(code, installation, now).length() > 0);
        } finally {
            System.clearProperty("lms.license.public-key");
        }
    }
}