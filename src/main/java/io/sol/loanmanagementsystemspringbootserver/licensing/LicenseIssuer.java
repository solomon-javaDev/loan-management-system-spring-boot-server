package io.sol.loanmanagementsystemspringbootserver.licensing;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Properties;

/** Run only on the provider's machine; never package its state directory with the customer app. */
public class LicenseIssuer {
    private final Path directory;
    private final Properties records = new Properties();
    private final PrivateKey key;

    public LicenseIssuer(Path directory) throws Exception {
        this.directory = directory;
        Files.createDirectories(directory);
        Path secret = directory.resolve("private.key");
        if (!Files.exists(secret)) {
            KeyPair pair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            Files.write(secret, pair.getPrivate().getEncoded());
            Files.writeString(directory.resolve("public.key"), Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
        }
        key = KeyFactory.getInstance("Ed25519").generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(secret)));
        if (Files.exists(directory.resolve("records.properties"))) {
            try (var input = Files.newInputStream(directory.resolve("records.properties"))) {
                records.load(input);
            }
        }
    }

    public synchronized String issue() throws Exception {
        byte[] random = new byte[24];
        new SecureRandom().nextBytes(random);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        records.setProperty("code." + code, "unused");
        save();
        return code;
    }

    public synchronized String activate(String code, String installation, Instant now) throws Exception {
        if (!installation.matches("[0-9a-fA-F-]{36}") || !records.containsKey("code." + code))
            throw new IllegalArgumentException("Unknown code or installation");
        String holder = records.getProperty("code." + code);
        if (!holder.equals("unused") && !holder.equals(installation))
            throw new IllegalArgumentException("Code already used by another installation");
        String existing = records.getProperty("expiry." + installation);
        Instant start = existing == null ? now : Instant.parse(existing);
        if (start.isBefore(now)) start = now;
        // Idempotent retries must not extend the term twice.
        if (holder.equals("unused")) {
            Instant expiry = ZonedDateTime.ofInstant(start, ZoneOffset.UTC).plusMonths(3).toInstant();
            records.setProperty("expiry." + installation, expiry.toString());
            records.setProperty("code." + code, installation);
            save();
        }
        String payload = installation + "|" + now.minusSeconds(60) + "|" + records.getProperty("expiry." + installation);
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(key);
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        signature.update(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes) + "."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
    }

    private void save() throws Exception {
        Path temp = Files.createTempFile(directory, "records-", ".tmp");
        try (var output = Files.newOutputStream(temp)) { records.store(output, "License activations"); }
        Files.move(temp, directory.resolve("records.properties"), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("Usage: <private-state-directory> issue | serve [port]");
        LicenseIssuer issuer = new LicenseIssuer(Path.of(args[0]));
        if (args[1].equals("issue")) {
            System.out.println(issuer.issue());
        } else if (args[1].equals("serve")) {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", args.length > 2 ? Integer.parseInt(args[2]) : 8099), 0);
            server.createContext("/activate", exchange -> {
                try {
                    if (!exchange.getRequestMethod().equals("POST")) throw new IllegalArgumentException("POST required");
                    String[] request = new String(exchange.getRequestBody().readNBytes(256), StandardCharsets.UTF_8).split("\\n", -1);
                    if (request.length != 2) throw new IllegalArgumentException("Invalid request");
                    byte[] response = issuer.activate(request[0].trim(), request[1].trim(), Instant.now()).getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                } catch (Exception e) {
                    exchange.sendResponseHeaders(403, -1);
                } finally { exchange.close(); }
            });
            server.start();
            System.out.println("Issuer listening on loopback; expose only through authenticated HTTPS reverse proxy.");
        } else throw new IllegalArgumentException("Unknown command");
    }
}