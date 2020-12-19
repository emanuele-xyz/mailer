package client;

public final class LoginResult {

    private final boolean result;
    private final String message;

    public LoginResult(boolean result, String message) {
        this.result = result;
        this.message = message;
    }

    public boolean getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}
