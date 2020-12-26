package mailer.messages;

import mailer.Mail;

public final class MailMessage extends Message {

    private final String json;

    public MailMessage(Mail mail) {
        super(MessageType.MAIL);
        json = mail.toJson();
    }

    public Mail getMail() {
        return Mail.fromJson(json);
    }
}
