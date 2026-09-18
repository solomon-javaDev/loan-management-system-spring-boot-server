package io.sol.loanmanagementsystemspringbootserver.utilities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class DocumentIdGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");

    private DocumentIdGenerator() {
    }

    public static String report(String code, String sourceId) {
        return "RPT-" + code + "-" + (sourceId == null || sourceId.isBlank() ? "GEN" : sourceId)
            + "-" + timestamp();
    }

    public static String receipt(String code, String sourceId) {
        return "RCT-" + code + "-" + (sourceId == null || sourceId.isBlank() ? "GEN" : sourceId)
            + "-" + timestamp();
        }

        private static String timestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT) + "-"
            + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}