package client;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import mailer.MailAddress;
import mailer.Utils;
import mailer.messages.LoginMessage;
import mailer.messages.ErrorMessage;
import mailer.messages.Message;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class LoginModel {

    private final SimpleStringProperty username;
    private final SimpleStringProperty errorMessage;
    private final SimpleBooleanProperty isLoggedIn;

    private final ServerDispatcher serverDispatcher;

    public LoginModel() throws UnknownHostException {
        username = new SimpleStringProperty();
        errorMessage = new SimpleStringProperty();
        isLoggedIn = new SimpleBooleanProperty(false);
        this.serverDispatcher = new ServerDispatcher();
    }

    public void close() {
        serverDispatcher.shutdown();
    }

    public void tryLogin() {
        String mailAddress = validateUsername();

        if (mailAddress != null) {
            LoginMessage msg = new LoginMessage(mailAddress);
            Future<Message> response = serverDispatcher.sendToServer(msg);
            Message message = getResult(response);
            if (message == null) {
                // Something went wrong during communication between client
                // and server. Anyway login has failed
                errorMessage.set("Server connection failure, please retry :(");
                return;
            }

            if (isLoginSuccessful(message)) {
                isLoggedIn.set(true);
            } else {
                ErrorMessage err = Utils.tryCast(ErrorMessage.class, message);
                if (err != null) {
                    errorMessage.set(err.getMessage());
                } else {
                    errorMessage.set("Login error!");
                }
            }

        } else {
            errorMessage.set("This is not a correct mail address :(");
        }
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public SimpleStringProperty errorMessageProperty() {
        return errorMessage;
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

    private static <T> T getResult(Future<T> future) {
        if (future == null) {
            return null;
        }

        T tmp = null;

        try {
            tmp = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return tmp;
    }

    private static boolean isLoginSuccessful(Message message) {
        switch (message.getType()) {
            case SUCCESS:
                return true;

            case ERROR:
            case LOGIN:
            default:
                return false;
        }
    }
}
