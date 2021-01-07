package mailer.messages;

/**
 * Message that a client sends in order to obtain mails from the server
 */
public final class MailFetchRequestMessage extends Message {

    private final String mailAddress;

    public MailFetchRequestMessage(String mailAddress) {
        super(MessageType.FETCH_REQUEST);
        this.mailAddress = mailAddress;
    }

    /**
     * @return the client's mail address
     */
    public String getMailAddress() {
        return mailAddress;
    }
}
