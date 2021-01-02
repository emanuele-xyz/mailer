package mailer.messages;

import mailer.Mail;
import mailer.MailAddress;

import java.util.UUID;

public final class MailDeleteMessage extends Message {

    private final MailAddress user;
    private final UUID mailID;

    public MailDeleteMessage(MailAddress user, UUID mailID) {
        super(MessageType.MAIL_DELETE);
        this.user = user;
        this.mailID = mailID;
    }

    public MailDeleteMessage(Mail mail) {
        this(mail.getFrom(), mail.getId());
    }

    public MailAddress getUser() {
        return user;
    }

    public UUID getMailID() {
        return mailID;
    }
}
