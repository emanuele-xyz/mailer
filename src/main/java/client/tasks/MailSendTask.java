package client.tasks;

import client.ServerDispatcher;
import client.logger.Logger;
import mailer.Mail;
import mailer.Utils;
import mailer.messages.ErrorMessage;
import mailer.messages.MailPushMessage;
import mailer.messages.Message;

import java.util.concurrent.Future;

public final class MailSendTask implements Runnable {

    private static final int MESSAGE_WAIT_TIME = 10 * 1000;

    private final Mail mail;
    private final ServerDispatcher serverDispatcher;
    private final Logger logger;
    private final MailSendCallback onSuccess;
    private final MailSendCallback onFinish;

    public MailSendTask(Mail mail, ServerDispatcher serverDispatcher, Logger logger, MailSendCallback onSuccess, MailSendCallback omFinish) {
        this.mail = mail;
        this.serverDispatcher = serverDispatcher;
        this.logger = logger;
        this.onSuccess = onSuccess;
        this.onFinish = omFinish;
    }

    @Override
    public void run() {
        Future<Message> message = serverDispatcher.sendToServer(new MailPushMessage(mail), MESSAGE_WAIT_TIME);
        Message response = Utils.getResult(message);
        if (message == null) {
            logger.error("Error sending mail to the server. Try later");
            return;
        }

        switch (response.getType()) {
            case SUCCESS: {
                logger.success("Mail successfully sent");
                onSuccess.exec();
            }
            break;

            case ERROR: {
                ErrorMessage tmp = Utils.tryCast(ErrorMessage.class, response);
                assert tmp != null;
                // If tmp where null it means that there is a mismatch between message class
                // and message type. This is a bug. We have to fix it in ErrorMessage class.

                logger.error(tmp.getMessage());
            }
            break;

            default:
                // This case should never be reached.
                // If it happens the server sends back an unexpected
                // message.
                // This is a server bug.
                assert false;
                break;
        }

        onFinish.exec();
    }
}
