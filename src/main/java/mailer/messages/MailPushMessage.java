package mailer.messages;

import mailer.Mail;

public final class MailPushMessage extends Message {

    private final Mail mail;

    public MailPushMessage(Mail mail) {
        super(MessageType.MAIL_PUSH);
        this.mail = mail;
    }

    public Mail getMail() {
        return mail;
    }
}