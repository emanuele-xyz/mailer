package client;

import mailer.Utils;
import mailer.messages.ErrorMessage;
import mailer.messages.LoginMessage;
import mailer.messages.Message;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class TryLoginTask implements Callable<LoginResult> {

    private static final int LOGIN_WAIT_TIME = 10;
    private static final TimeUnit LOGIN_WAIT_TIME_UNIT = TimeUnit.SECONDS;

    private final String mailAddress;
    private final ServerDispatcher serverDispatcher;

    public TryLoginTask(String mailAddress, ServerDispatcher serverDispatcher) {
        this.mailAddress = mailAddress;
        this.serverDispatcher = serverDispatcher;
    }

    @Override
    public LoginResult call() {

        LoginMessage msg = new LoginMessage(mailAddress);
        Future<Message> response = serverDispatcher.sendToServer(msg);
        // this blocks the thread!
        Message message = Utils.getResult(response, LOGIN_WAIT_TIME, LOGIN_WAIT_TIME_UNIT);
        if (message == null) {
            // Something went wrong during communication between client
            // and server. Anyway login has failed
            return new LoginResult(false, "Server connection failure, please retry :(");
        }

        if (isLoginSuccessful(message)) {
            return new LoginResult(true, "Success!");
        } else {
            ErrorMessage err = Utils.tryCast(ErrorMessage.class, message);
            return new LoginResult(false, err != null ? err.getMessage() : "Login error!");
        }
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
