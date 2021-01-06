package server.logger;

import mailer.messages.Message;

/**
 * Logger interface
 */
public interface Logger {

    /**
     * Log a message
     * @param message the message to be logged
     */
    void print(String message);

    /**
     * Format and log a message
     * @param format format string
     * @param args arguments referenced by the format specifiers in the format string
     */
    void print(String format, Object... args);
}
