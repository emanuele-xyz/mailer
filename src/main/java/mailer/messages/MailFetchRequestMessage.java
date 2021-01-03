package mailer.messages;

public final class MailFetchRequestMessage extends Message {

    private final String mailAddress;

    public MailFetchRequestMessage(String mailAddress) {
        super(MessageType.FETCH_REQUEST);
        this.mailAddress = mailAddress;
    }

    public String getMailAddress() {
        return mailAddress;
    }
}
