package server.logger;

public interface Logger {

    void print(String message);
    void print(String message, Object... args);
}
