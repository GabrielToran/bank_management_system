package com.bankapp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class AuditLogger {
    private static final Logger LOGGER = Logger.getLogger("BankAuditLogger");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static {
        try {
            FileHandler fileHandler = new FileHandler("bank_audit.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Log an audit message
     *
     * @param username The username
     * @param message The audit message
     */
    public static void log(String username, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        LOGGER.info(String.format("[%s] User: %s - %s", timestamp, username, message));
    }
}
