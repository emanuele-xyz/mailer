package client.tasks;

import client.Logger;
import client.ServerDispatcher;
import mailer.MailAddress;
import mailer.Utils;
import mailer.messages.ErrorMessage;
import mailer.messages.MailDeleteMessage;
import mailer.messages.Message;
import mailer.messages.Success;

import java.util.UUID;
import java.util.concurrent.Future;

public final class MailDeleteTask implements Runnable {

    private static final int MESSAGE_WAIT_TIME = 10 * 1000;

    private final ServerDispatcher serverDispatcher;
    private final Logger logger;
    private final MailAddress user;
    private final UUID mailID;
    private final MailDeleteCallback onSuccess;

    public MailDeleteTask(ServerDispatcher serverDispatcher, Logger logger, MailAddress user, UUID mailID, MailDeleteCallback onSuccess) {
        this.serverDispatcher = serverDispatcher;
        this.logger = logger;
        this.user = user;
        this.mailID = mailID;
        this.onSuccess = onSuccess;
    }

    @Override
    public void run() {
        Future<Message> message = serverDispatcher.sendToServer(new MailDeleteMessage(user, mailID), MESSAGE_WAIT_TIME);
        Message response = Utils.getResult(message);
        if (response == null) {
            logger.print("Error sending delete message to server! Try again");
            return;
        }

        switch (response.getType()) {
            case ERROR: {
                ErrorMessage tmp = Utils.tryCast(ErrorMessage.class, response);
                assert tmp != null;
                // If tmp where null it means that there is a mismatch between message class
                // and message type. This is a bug. We have to fix it in ErrorMessage class.

                logger.print(tmp.getMessage());
            }
            break;

            case SUCCESS: {
                Success tmp = Utils.tryCast(Success.class, response);
                assert tmp != null;
                // If tmp where null it means that there is a mismatch between message class
                // and message type. This is a bug. We have to fix it in Success class.

                logger.print("Successfully deleted mail");

                onSuccess.exec();
            }
            break;

            default: {
                // This should not happen, if it does it's a programmer error!
                // Fix this by sending back a message that is expected
                System.err.println("Server sends an incorrect response message for a fetch request message");
                assert false;
            }
            break;
        }
    }
}
