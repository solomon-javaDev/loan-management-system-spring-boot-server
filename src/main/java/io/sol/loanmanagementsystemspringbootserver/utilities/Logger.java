package io.sol.loanmanagementsystemspringbootserver.utilities;

import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class Logger {
    private static final String LOG_FILE = "system.log";
    private static final String LOG_FILE_ENC = "system.log.enc";
    private static final String LOG_FILE_DEC = "system.log.dec";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public enum LogLevel {
        INFO, WARNING, ERROR
    }

    private static LogLevel currentLevel = LogLevel.INFO;

    public static void setLogLevel(LogLevel level) {
        currentLevel = level;
    }

    private static String getSource() {
        return StackWalker.getInstance()
                .walk(frames -> frames
                        .map(f -> f.getClassName() + "." + f.getMethodName())
                        .filter(name -> !name.startsWith("io.sol.loanmanagementsystemspringbootserver.utilities.Logger") &&
                                !name.startsWith("io.sol.loanmanagementsystemspringbootserver.utilities.Result") &&
                                !name.startsWith("io.sol.loanmanagementsystemspringbootserver.utilities.GlobalExceptionHandler"))
                        .findFirst()
                        .orElse("UnknownSource"));
    }

    private static void logToFile(String message) {
        String timestamp = dateFormat.format(new Date());
        String source = getSource();
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(timestamp + " [" + source + "] " + message);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
        try {
            FileEncryptor.encryptFile(LOG_FILE, LOG_FILE + ".enc");
            // new File(LOG_FILE).delete();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());

        }
    }

    public static void showLogs(){
        try{
            FileEncryptor.decrypt(LOG_FILE_ENC, LOG_FILE_DEC);

        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());

        }
    }

    public static void logInfo(String message) {
        if (currentLevel.ordinal() <= LogLevel.INFO.ordinal()) {
            logToFile("INFO: " + message);
        }
    }

    public static void logWarning(String message) {
        if (currentLevel.ordinal() <= LogLevel.WARNING.ordinal()) {
            logToFile("WARNING: " + message);
        }
    }

    public static void logError(String message) {
        if (currentLevel.ordinal() <= LogLevel.ERROR.ordinal()) {
            logToFile("ERROR: " + message);
        }
    }
}
