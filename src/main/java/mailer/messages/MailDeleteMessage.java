package mailer.messages;

import mailer.Mail;

import java.util.UUID;

public final class MailDeleteMessage extends Message {

    private final UUID mailID;

    public MailDeleteMessage(UUID mailID) {
        super(MessageType.MAIL_DELETE);
        this.mailID = mailID;
    }

    public MailDeleteMessage(Mail mail) {
        this(mail.getId());
    }

    public UUID getMailID() {
        return mailID;
    }
}
