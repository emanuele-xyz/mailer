package mailer.messages;

import mailer.Mail;

public final class MailFetchResponseMessage extends Message {

    private final Mail[] mails;

    public MailFetchResponseMessage(Mail ... mails) {
        super(MessageType.FETCH_RESPONSE);
        this.mails = mails;
    }

    public MailFetchResponseMessage(MessageType type, Mail[] mails) {
        super(type);
        this.mails = mails;
    }

    public Mail[] getMails() {
        return mails;
    }
}
