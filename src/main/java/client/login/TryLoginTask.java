package client.login;

import client.ServerDispatcher;
import mailer.Utils;
import mailer.messages.ErrorMessage;
import mailer.messages.LoginMessage;
import mailer.messages.Message;
import mailer.messages.MessageType;

import java.util.concurrent.Future;

/**
 * Task that sends to the server a login request
 */
public final class TryLoginTask implements Runnable {

    private static final int WAIT_TIME = 10 * 1000;

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
        Future<Message> response = serverDispatcher.sendToServer(msg, WAIT_TIME);
        Message message = Utils.getResult(response);
        if (message == null) {
            // Something went wrong during communication between client and server.
            // Login has failed
            onResult.run(false, "Server connection failure. Try later");
            return;
        }

        if (isLoginSuccessful(message)) {
            onResult.run(true, "");
        } else {
            ErrorMessage err = Utils.tryCast(ErrorMessage.class, message);
            onResult.run(false, err != null ? err.getMessage() : "Login error!");
        }
    }

    private static boolean isLoginSuccessful(Message message) {
        return message.getType() == MessageType.SUCCESS;
    }
}
