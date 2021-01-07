package mailer.messages;

import mailer.Mail;

/**
 * Message sent from the server in response to a <code>MailFetchRequestMessage</code>.
 * It contains all the mails requested by the client
 */
public final class MailFetchResponseMessage extends Message {

    private final Mail[] mails;

    public MailFetchResponseMessage(Mail[] mails) {
        super(MessageType.FETCH_RESPONSE);
        this.mails = mails;
    }

    /**
     * @return mails requested by the client
     */
    public Mail[] getMails() {
        return mails;
    }
}
