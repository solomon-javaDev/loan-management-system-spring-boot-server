package io.sol.loanmanagementsystemspringbootserver.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class DatabaseBackupService {

    private final JdbcTemplate jdbcTemplate;
    private final Path directory;

    @Autowired
    public DatabaseBackupService(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, Path.of(System.getProperty("user.dir"), "data"));
    }

    public DatabaseBackupService(JdbcTemplate jdbcTemplate, Path directory) {
        this.jdbcTemplate = jdbcTemplate;
        this.directory = directory;
    }

    @Scheduled(initialDelayString = "60000", fixedDelayString = "${lms.backup.interval-ms:3600000}")
    public void backup() throws IOException {
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "loan_db-backup-", ".zip");
        try {
            Files.delete(temporary);
            jdbcTemplate.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) connection -> {
                try (var statement = connection.createStatement()) {
                    statement.execute("BACKUP TO '" + temporary.toAbsolutePath().toString().replace("'", "''") + "'");
                }
                return null;
            });
            Files.move(temporary, directory.resolve("loan_db-backup.zip"), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}