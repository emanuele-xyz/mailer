package mailer.messages;

/**
 * Message that the client sends to the server to request login
 */
public final class LoginMessage extends Message {

    private final String mailAddress;

    public LoginMessage(String mailAddress) {
        super(MessageType.LOGIN);
        this.mailAddress = mailAddress;
    }

    /**
      * @return the client's mail address
     */
    public String getMailAddress() {
        return mailAddress;
    }
}
