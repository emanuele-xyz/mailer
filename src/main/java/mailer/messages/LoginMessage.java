package mailer.messages;

public final class LoginMessage extends Message {

    private final String mailAddress;

    public LoginMessage(String mailAddress) {
        super(MessageType.LOGIN);
        this.mailAddress = mailAddress;
    }

    public String getMailAddress() {
        return mailAddress;
    }

}
