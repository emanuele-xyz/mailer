package client;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import mailer.MailAddress;

import java.net.UnknownHostException;
import java.util.concurrent.*;

public final class LoginModel {

    private static final int LOGIN_TIMEOUT = 10;
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

        // If login flag is set to false it means that there was no
        // successful login, hence we won't be transitioning to the next scene
        if (!isLoggedIn.get()) {
            serverDispatcher.shutdown();
        }

        loginExecutor.shutdown();
    }

    public void tryLogin() {
        String mailAddress = validateUsername();
        if (mailAddress == null) {
            errorMessage.set("This is not a correct mail address :(");
            return;
        }

        isLoginButtonDisabled.set(true);
        Callable<LoginResult> tryLoginTask = new TryLoginTask(mailAddress, serverDispatcher);
        Future<LoginResult> tryLoginResult = loginExecutor.submit(tryLoginTask);
        try {

            // TODO: this blocks the javafx thread, which is not a good idea
            // TODO: use event driven programming to avoid this!
            // TODO: add a synchronized boolean property, share it with login task, set its change event
            // TODO: to signal that the login task has finished
            LoginResult loginResult = tryLoginResult.get();
            if (loginResult.getResult()) {
                isLoggedIn.set(true);
            } else {
                errorMessage.set(loginResult.getMessage());
            }

        } catch (InterruptedException | ExecutionException e) {
            errorMessage.set("Login error!");
            e.printStackTrace();
        }
        isLoginButtonDisabled.set(false);
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

    public ServerDispatcher getServerDispatcher() {
        return serverDispatcher;
    }

    private String validateUsername() {
        // Trim the input text since user can mistype some
        // spaces at the start and at the end of the input field
        String tmp = username.get().trim();
        if (MailAddress.validate(tmp)) {
            return tmp;
        } else {
            return null;
        }
    }
}
