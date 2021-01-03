package mailer.messages;

import java.util.UUID;

public final class MailFetchRequestMessage extends Message {

    private final String mailAddress;
    private final UUID[] received;

    public MailFetchRequestMessage(String mailAddress, UUID ... received) {
        super(MessageType.FETCH_REQUEST);
        this.mailAddress = mailAddress;
        this.received = received;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public UUID[] getReceived() {
        return received;
    }
}
