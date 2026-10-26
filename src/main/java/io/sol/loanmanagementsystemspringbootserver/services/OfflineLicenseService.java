package io.sol.loanmanagementsystemspringbootserver.services;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.prefs.Preferences;

@Service
public class OfflineLicenseService {
    private final Preferences store = Preferences.userRoot().node("io/sol/loanmanagementsystem/license");

    public String installationId() {
        String id = store.get("installation", null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            store.put("installation", id);
        }
        return id;
    }

    public boolean isAllowed() {
        return valid(store.get("license", ""), Instant.now());
    }

    public boolean valid(String license, Instant now) {
        try {
            String[] parts = license.split("\\.", -1);
            if (parts.length != 2) return false;
            byte[] payload = Base64.getUrlDecoder().decode(parts[0]);
            byte[] signed = Base64.getUrlDecoder().decode(parts[1]);
            String publicKey = System.getProperty("lms.license.public-key", "");
            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(KeyFactory.getInstance("Ed25519").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey))));
            verifier.update(payload);
            if (!verifier.verify(signed)) return false;
            String[] fields = new String(payload, StandardCharsets.UTF_8).split("\\|", -1);
            return fields.length == 3 && fields[0].equals(installationId())
                    && !now.isBefore(Instant.parse(fields[1])) && now.isBefore(Instant.parse(fields[2]));
        } catch (Exception e) {
            return false;
        }
    }

    public void activate(String code) throws Exception {
        String url = System.getProperty("lms.license.url", "");
        if (!url.startsWith("https://")) throw new IllegalStateException("Configure the HTTPS activation URL first.");
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(15))
                .header("Content-Type", "text/plain; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(code.trim() + "\n" + installationId())).build();
        HttpResponse<String> response = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(10))
                .build().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 || !valid(response.body().trim(), Instant.now()))
            throw new IllegalArgumentException("Activation was refused or the returned license is invalid.");
        store.put("license", response.body().trim());
        store.flush();
    }
}