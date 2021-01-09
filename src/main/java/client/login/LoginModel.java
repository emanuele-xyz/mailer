package client.login;

import client.ServerDispatcher;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import mailer.MailAddress;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * LoginModel handles login logic
 */
public final class LoginModel {

    private static final int LOGIN_EXECUTOR_THREADS = 1;

    private final SimpleStringProperty username;
    private final SimpleStringProperty errorMessage;
    private final SimpleBooleanProperty isLoggingIn;
    private final SimpleBooleanProperty isLoggedIn;

    private final ServerDispatcher serverDispatcher;
    private final ExecutorService loginExecutor;

    public LoginModel() throws UnknownHostException {
        username = new SimpleStringProperty();
        errorMessage = new SimpleStringProperty();
        isLoggingIn = new SimpleBooleanProperty(false);
        isLoggedIn = new SimpleBooleanProperty(false);

        serverDispatcher = new ServerDispatcher();
        loginExecutor = Executors.newFixedThreadPool(LOGIN_EXECUTOR_THREADS);
    }

    /**
     * Close the model
     */
    public void close() {
        serverDispatcher.shutdown();
        loginExecutor.shutdown();
    }

    /**
     * Try to login
     */
    public void tryLogin() {
        String mailAddress = validateUsername();
        if (mailAddress == null) {
            errorMessage.set("Invalid mail address :(");
            return;
        }

        isLoggingIn.set(true);
        TryLoginCallback onResult = (result, msg) -> {
            if (result) {
                Platform.runLater(() -> isLoggedIn.set(true));
            } else {
                Platform.runLater(() -> errorMessage.set(msg));
            }
            Platform.runLater(() -> isLoggingIn.set(false));
        };
        Runnable tryLoginTask = new TryLoginTask(mailAddress, serverDispatcher, onResult);
        loginExecutor.submit(tryLoginTask);
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public SimpleStringProperty errorMessageProperty() {
        return errorMessage;
    }

    public SimpleBooleanProperty isLoggingInProperty() {
        return isLoggingIn;
    }

    public SimpleBooleanProperty isLoggedInProperty() {
        return isLoggedIn;
    }

    public boolean isLoggedIn() {
        return isLoggedIn.get();
    }

    public String getUsername() {
        return username.get().trim();
    }

    /**
     * Checks if the username is a valid mail address
     *
     * @return the trimmed username string if valid, null otherwise
     */
    private String validateUsername() {
        String tmp = getUsername();
        if (MailAddress.validate(tmp)) {
            return tmp;
        } else {
            return null;
        }
    }
}
