package client.logger;

public interface Logger {

    void success(String message);

    void success(String message, Object... args);

    void error(String message);

    void error(String message, Object... args);
}
