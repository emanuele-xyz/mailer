package client.tasks;

import client.ServerDispatcher;
import client.logger.Logger;
import mailer.MailAddress;
import mailer.Utils;
import mailer.messages.ErrorMessage;
import mailer.messages.MailDeleteMessage;
import mailer.messages.Message;
import mailer.messages.Success;

import java.util.UUID;
import java.util.concurrent.Future;

public final class MailDeleteTask extends Task {

    private final MailAddress user;
    private final UUID mailID;
    private final MailDeleteCallback onSuccess;

    public MailDeleteTask(ServerDispatcher serverDispatcher, Logger logger, MailAddress user, UUID mailID, MailDeleteCallback onSuccess) {
        super(serverDispatcher, logger);
        this.user = user;
        this.mailID = mailID;
        this.onSuccess = onSuccess;
    }

    @Override
    public void run() {
        Future<Message> message = serverDispatcher.sendToServer(new MailDeleteMessage(user, mailID), MESSAGE_WAIT_TIME);
        Message response = Utils.getResult(message);
        if (response == null) {
            logger.error("Error sending delete message to server! Try again");
            return;
        }

        switch (response.getType()) {
            case ERROR: {
                ErrorMessage tmp = Utils.cast(ErrorMessage.class, response);
                logger.error(tmp.getMessage());
            }
            break;

            case SUCCESS: {
                Success tmp = Utils.cast(Success.class, response);
                logger.success("Successfully deleted mail");
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
