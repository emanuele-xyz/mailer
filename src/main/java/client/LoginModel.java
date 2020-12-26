package client;

import client.login.TryLoginCallback;
import client.login.TryLoginTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import mailer.MailAddress;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LoginModel {

    private static final int LOGIN_EXECUTOR_THREADS = 1;

    private final SimpleStringProperty username;
    private final SimpleStringProperty errorMessage;
    private final SimpleBooleanProperty isLoginButtonDisabled;
    private final SimpleBooleanProperty isLoggedIn;

    private final ServerDispatcher serverDispatcher;
    private final ExecutorService loginExecutor;

    public LoginModel() throws UnknownHostException {
        username = new SimpleStringProperty();
        errorMessage = new SimpleStringProperty();
        isLoginButtonDisabled = new SimpleBooleanProperty(false);
        isLoggedIn = new SimpleBooleanProperty(false);

        serverDispatcher = new ServerDispatcher();
        loginExecutor = Executors.newFixedThreadPool(LOGIN_EXECUTOR_THREADS);
    }

    public void close() {
        serverDispatcher.shutdown();
        loginExecutor.shutdown();
    }

    public void tryLogin() {
        String mailAddress = validateUsername();
        if (mailAddress == null) {
            errorMessage.set("This is not a correct mail address :(");
            return;
        }

        isLoginButtonDisabled.set(true);
        TryLoginCallback onResult = (result, msg) -> {
            if (result) {
                Platform.runLater(() -> isLoggedIn.set(true));
            } else {
                Platform.runLater(() -> errorMessage.set(msg));
            }
            Platform.runLater(() -> isLoginButtonDisabled.set(false));
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

    public SimpleBooleanProperty isLoginButtonDisabledProperty() {
        return isLoginButtonDisabled;
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

    private String validateUsername() {
        // Trim the input text since user can mistype some
        // spaces at the start and at the end of the input field
        String tmp = getUsername();
        if (MailAddress.validate(tmp)) {
            return tmp;
        } else {
            return null;
        }
    }
}
