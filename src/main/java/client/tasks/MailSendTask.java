package client.tasks;

import client.Logger;
import client.MailDraftProperty;
import client.ServerDispatcher;
import mailer.Mail;

import java.util.function.Consumer;

public final class MailSendTask implements Runnable {

    private static final int MESSAGE_WAIT_TIME = 10 * 1000;

    private final Mail mail;
    private final ServerDispatcher serverDispatcher;
    private final Logger logger;
    private final Consumer<MailDraftProperty> onSuccess;

    public MailSendTask(Mail mail, ServerDispatcher serverDispatcher, Logger logger, Consumer<MailDraftProperty> onSuccess) {
        this.mail = mail;
        this.serverDispatcher = serverDispatcher;
        this.logger = logger;
        this.onSuccess = onSuccess;
    }

    @Override
    public void run() {
        // TODO: implement email send
    }
}
