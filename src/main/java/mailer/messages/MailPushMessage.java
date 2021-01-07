package mailer.messages;

import mailer.Mail;

/**
 * Message that clients send to the server.
 * It means that the client is trying to send a mail
 */
public final class MailPushMessage extends Message {

    private final Mail mail;

    public MailPushMessage(Mail mail) {
        super(MessageType.MAIL_PUSH);
        this.mail = mail;
    }

    /**
     * @return the mail sent from a client
     */
    public Mail getMail() {
        return mail;
    }
}