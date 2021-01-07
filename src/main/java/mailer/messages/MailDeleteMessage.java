package mailer.messages;

import mailer.MailAddress;

import java.util.UUID;

/**
 * Message sent from the client to request for a mail deletion
 */
public final class MailDeleteMessage extends Message {

    private final MailAddress user;
    private final UUID mailID;

    public MailDeleteMessage(MailAddress user, UUID mailID) {
        super(MessageType.MAIL_DELETE);
        this.user = user;
        this.mailID = mailID;
    }

    /**
     * @return the client's mail address
     */
    public MailAddress getUser() {
        return user;
    }

    /**
     * @return the id of the mail to be deleted
     */
    public UUID getMailID() {
        return mailID;
    }
}
