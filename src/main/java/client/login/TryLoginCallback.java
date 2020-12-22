package client.login;

public interface TryLoginCallback {
    void run(boolean loginResult, String message);
}
