package client.tasks;

import client.ServerDispatcher;
import client.logger.Logger;
import mailer.MailAddress;
import mailer.Utils;
import mailer.messages.ErrorMessage;
import mailer.messages.MailDeleteMessage;
import mailer.messages.Message;

import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Task that handles a mail delete message
 */
public final class MailDeleteTask extends Task {

    private final MailAddress user;
    private final UUID mailID;
    private final MailDeleteCallback onSuccess;
    private final MailDeleteCallback onFinish;

    public MailDeleteTask(ServerDispatcher serverDispatcher, Logger logger, MailAddress user, UUID mailID, MailDeleteCallback onSuccess, MailDeleteCallback onFinish) {
        super(serverDispatcher, logger);
        this.user = user;
        this.mailID = mailID;
        this.onSuccess = onSuccess;
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        Future<Message> message = serverDispatcher.sendToServer(new MailDeleteMessage(user, mailID), MESSAGE_WAIT_TIME);
        Message response = Utils.getResult(message);
        if (response == null) {
            logger.error("Error sending delete message to server! Try again");
            onFinish.exec();
            return;
        }

        switch (response.getType()) {
            case SUCCESS: {
                logger.success("Successfully deleted mail");
                onSuccess.exec();
            }
            break;

            case ERROR: {
                ErrorMessage tmp = Utils.cast(ErrorMessage.class, response);
                logger.error(tmp.getMessage());
            }
            break;

            default: {
                // This should not happen, if it does it's a programmer error!
                System.err.printf("Received unexpected '%s' message in response to mail delete message\n", response.getType());
                assert false;
            }
            break;
        }

        onFinish.exec();
    }
}
