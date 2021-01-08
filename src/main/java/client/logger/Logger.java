package client.logger;

/**
 * Logger logs messages to a generic output
 */
public interface Logger {

    /**
     * Log success message
     * @param message the message to log
     */
    void success(String message);

    /**
     * Format and log success message
     * @param message success message format string
     * @param args arguments referenced by the format specifiers in the format string
     */
    void success(String message, Object... args);

    /**
     * Logs an error message
     * @param message the message to log
     */
    void error(String message);

    /**
     * Format and log error message
     * @param message error message format string
     * @param args arguments referenced by the format specifiers in the format string
     */
    void error(String message, Object... args);
}
