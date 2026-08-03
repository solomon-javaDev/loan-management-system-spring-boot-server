package io.sol.loanmanagementsystemspringbootserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The ActivityLoggingService class provides functionality for logging system activities
 * performed by users. It logs information to both the console and a dedicated log file.
 *
 * Responsibilities:
 * - Logs activity details including timestamp, username, and activity description.
 * - Writes log entries to a standard logger for console output.
 * - Persists log entries to a file for long-term storage and reference.
 *
 * Key Features:
 * - Uses a consistent logging format with timestamps for clarity.
 * - Handles file write operations to ensure activities are persistently logged.
 * - Logs errors that occur during the file-writing process for troubleshooting.
 */
@Service
public class ActivityLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLoggingService.class);
    private static final String LOG_FILE = "system_activity.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void logActivity(String user, String activity) {
        String logEntry = String.format("[%s] USER: %s | ACTIVITY: %s",
                LocalDateTime.now().format(formatter), user, activity);
        
        // Log to console/standard logger
        logger.info(logEntry);
        
        // Log to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(logEntry);
            writer.newLine();
        } catch (IOException e) {
            logger.error("Failed to write to log file: " + e.getMessage());
        }
    }
}
