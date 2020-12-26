package mailer.messages;

public final class MailFetchRequestMessage extends Message {

    public MailFetchRequestMessage() {
        super(MessageType.FETCH_REQUEST);
    }
}
