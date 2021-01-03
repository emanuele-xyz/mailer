package client.tasks;

import mailer.Mail;

import java.util.List;

public interface MailFetchCallback {
    void exec(List<Mail> mail);
}
