package client.tasks;

import mailer.Mail;

public interface MailFetchCallback {
    void exec(Mail mail);
}
