package client;

public interface TryLoginCallback {
    void run(boolean loginResult, String message);
}
