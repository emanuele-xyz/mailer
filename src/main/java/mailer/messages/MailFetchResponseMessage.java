package mailer.messages;

public final class MailFetchResponseMessage extends Message {

    private final int count;

    public MailFetchResponseMessage(int count) {
        super(MessageType.FETCH_RESPONSE);
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
