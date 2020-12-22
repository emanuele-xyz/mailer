package client;

import mailer.Utils;
import mailer.messages.ErrorMessage;
import mailer.messages.LoginMessage;
import mailer.messages.Message;

import java.util.concurrent.Future;

public final class TryLoginTask implements Runnable {

    private static final int LOGIN_WAIT_TIME = 10 * 1000;

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
        Future<Message> response = serverDispatcher.sendToServer(msg, LOGIN_WAIT_TIME);
        Message message = Utils.getResult(response);
        if (message == null) {
            // Something went wrong during communication between client
            // and server. Login has failed
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
