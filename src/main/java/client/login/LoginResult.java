package client.login;

/**
 * LoginResult models the result of the login ui
 */
public final class LoginResult {

    private final boolean result;
    private final String address;

    public LoginResult(boolean result, String address) {
        this.result = result;
        this.address = address;
    }

    /**
     * @return true if user logged in, false otherwise
     */
    public boolean isSuccessful() {
        return result;
    }

    /**
     * Meaningful only if <code>isSuccessful</code> returns <code>true</code>
     *
     * @return the logged in user mail address
     */
    public String getAddress() {
        return address;
    }
}
