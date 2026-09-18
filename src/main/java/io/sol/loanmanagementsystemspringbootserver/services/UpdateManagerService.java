package io.sol.loanmanagementsystemspringbootserver.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
public class UpdateManagerService {

    private final String GITHUB_API_URL = "https://api.github.com/repos/solomon-javaDev/loan-management-system-spring-boot-server/releases/latest";

    @Value("${spring.application.version}")
     private String currentVersion;

    private final RestTemplate restTemplate = new RestTemplate();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubRelease {
        @JsonProperty("tag_name")
        private String tagName;
        private List<GitHubAsset> assets;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubAsset {
        private String name;
        @JsonProperty("browser_download_url")
        private String browserDownloadUrl;
    }

    /**
     * Polls GitHub for updates every 6 hours.
     * Initial delay is set to 1 minute to allow the app to fully start.
     */
    @Scheduled(fixedRate = 21600000, initialDelay = 60000)
    public void checkForUpdates() {
        log.info("Checking for system updates...");
        try {
            ResponseEntity<GitHubRelease> response = restTemplate.getForEntity(GITHUB_API_URL, GitHubRelease.class);
            GitHubRelease release = response.getBody();
            
            if (release != null && isNewerVersion(release.getTagName(), currentVersion)) {
                log.info("New version detected: {}. Current version: {}. Starting download...", release.getTagName(), currentVersion);
                
                if (release.getAssets() != null) {
                    for (GitHubAsset asset : release.getAssets()) {
                        if (asset.getName() != null && asset.getName().endsWith(".jar")) {
                            downloadUpdate(asset.getBrowserDownloadUrl());
                            break;
                        }
                    }
                }
            } else {
                log.info("System is up to date (Current normalized version: {})", normalizeVersion(currentVersion));
            }
        } catch (Exception e) {
            log.error("Silent failure during update check: {}", e.getMessage());
        }
    }

    private boolean isNewerVersion(String latest, String current) {
        if (latest == null || latest.isEmpty()) return false;

        String normalizedLatest = normalizeVersion(latest);
        String normalizedCurrent = normalizeVersion(current);

        if (normalizedLatest.equals(normalizedCurrent)) return false;

        String[] latestParts = normalizedLatest.split("\\.");
        String[] currentParts = normalizedCurrent.split("\\.");

        int length = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            try {
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i].replaceAll("\\D", "")) : 0;
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i].replaceAll("\\D", "")) : 0;

                if (latestPart > currentPart) return true;
                if (latestPart < currentPart) return false;
            } catch (NumberFormatException e) {
                // If not numeric, fallback to string comparison for this part
                String l = i < latestParts.length ? latestParts[i] : "";
                String c = i < currentParts.length ? currentParts[i] : "";
                int comp = l.compareTo(c);
                if (comp > 0) return true;
                if (comp < 0) return false;
            }
        }

        return false;
    }

    private String normalizeVersion(String version) {
        if (version == null) return "";
        
        String normalized = version.trim().toLowerCase();
        
        // Strip leading 'v'
        if (normalized.startsWith("v")) {
            normalized = normalized.substring(1);
        }
        
        // Strip '-snapshot'
        if (normalized.endsWith("-snapshot")) {
            normalized = normalized.substring(0, normalized.length() - "-snapshot".length());
        }
        
        return normalized.trim();
    }

    private Path getApplicationDirectory() {
        try {
            URI uri = UpdateManagerService.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path path = Paths.get(uri);
            
            // If running from a JAR, the location is the JAR file itself. We want the parent directory.
            if (Files.isRegularFile(path)) {
                return path.getParent();
            }
            return path;
        } catch (Exception e) {
            log.warn("Could not resolve absolute execution path via URI, falling back to default: {}", e.getMessage());
            return Paths.get(".").toAbsolutePath().normalize();
        }
    }

    private void downloadUpdate(String downloadUrl) {
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Path appDir = getApplicationDirectory();
                Path stagingDir = appDir.resolve("updates").resolve("staging");
                if (!Files.exists(stagingDir)) {
                    Files.createDirectories(stagingDir);
                }
                
                Path tempPath = stagingDir.resolve("update.jar.tmp");
                Path targetPath = appDir.resolve("update.jar");
                
                log.info("Downloading update to staging: {}", tempPath.toAbsolutePath());
                
                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(tempPath.toFile())) {
                    
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                
                // Verify JAR integrity
                if (isValidJar(tempPath)) {
                    Files.move(tempPath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    log.info("Update verified and ready: {}. It will be applied on next restart.", targetPath.toAbsolutePath());
                } else {
                    log.error("Downloaded update is not a valid JAR file. Deleting temp file.");
                    Files.deleteIfExists(tempPath);
                }
            } else {
                log.warn("Failed to download update. Server responded with: {}", connection.getResponseCode());
            }
        } catch (Exception e) {
            log.error("Failure during update download: {}", e.getMessage());
        }
    }

    private boolean isValidJar(Path path) {
        try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(path.toFile())) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
