package client;

import mailer.Utils;
import mailer.messages.ErrorMessage;
import mailer.messages.LoginMessage;
import mailer.messages.Message;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class TryLoginTask implements Runnable {

    private static final int LOGIN_WAIT_TIME = 10;
    private static final TimeUnit LOGIN_WAIT_TIME_UNIT = TimeUnit.SECONDS;

    private final String mailAddress;
    private final ServerDispatcher serverDispatcher;
    private final TryLoginCallback onResult;

    public TryLoginTask(String mailAddress, ServerDispatcher serverDispatcher, TryLoginCallback onResult) {
        this.mailAddress = mailAddress;
        this.serverDispatcher = serverDispatcher;
        this.onResult = onResult;
    }

    @Override
    public void run() {
        LoginMessage msg = new LoginMessage(mailAddress);
        Future<Message> response = serverDispatcher.sendToServer(msg);
        Message message = Utils.getResult(response, LOGIN_WAIT_TIME, LOGIN_WAIT_TIME_UNIT);
        if (message == null) {
            // Something went wrong during communication between client
            // and server. Anyway login has failed
            onResult.run(false, "Server connection failure :(");
            return;
        }

        if (isLoginSuccessful(message)) {
            onResult.run(true, "Success!");
        } else {
            ErrorMessage err = Utils.tryCast(ErrorMessage.class, message);
            onResult.run(false, err != null ? err.getMessage() : "Login error!");
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
