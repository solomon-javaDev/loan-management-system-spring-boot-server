package io.sol.loanmanagementsystemspringbootserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
