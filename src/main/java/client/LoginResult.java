package client;

public final class LoginResult {

    private final boolean result;
    private final String account;

    public LoginResult(boolean result, String account) {
        this.result = result;
        this.account = account;
    }

    public boolean isSuccessful() {
        return result;
    }

    public String getAccount() {
        return account;
    }
}
