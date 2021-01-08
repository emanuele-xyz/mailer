package client.tasks;

import client.ServerDispatcher;
import client.logger.Logger;
import mailer.Mail;
import mailer.Utils;
import mailer.messages.ErrorMessage;
import mailer.messages.MailPushMessage;
import mailer.messages.Message;

import java.util.concurrent.Future;

/**
 * Task that handles a mail send message
 */
public final class MailSendTask extends Task {

    private final Mail mail;
    private final MailSendCallback onSuccess;
    private final MailSendCallback onFinish;

    public MailSendTask(ServerDispatcher serverDispatcher, Logger logger, Mail mail, MailSendCallback onSuccess, MailSendCallback onFinish) {
        super(serverDispatcher, logger);
        this.mail = mail;
        this.onSuccess = onSuccess;
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        Future<Message> message = serverDispatcher.sendToServer(new MailPushMessage(mail), MESSAGE_WAIT_TIME);
        Message response = Utils.getResult(message);
        if (message == null) {
            logger.error("Error sending mail to the server. Try later");
            onFinish.exec();
            return;
        }

        switch (response.getType()) {
            case SUCCESS: {
                logger.success("Mail successfully sent");
                onSuccess.exec();
            }
            break;

            case ERROR: {
                ErrorMessage tmp = Utils.cast(ErrorMessage.class, response);
                logger.error(tmp.getMessage());
            }
            break;

            default:
                // This case should never be reached
                // If it happens the server has sent back an unexpected message
                // This is a programmer error
                System.err.printf("Received unexpected '%s' message in response to mail push message\n", response.getType());
                assert false;
                break;
        }

        onFinish.exec();
    }
}
