package mailer.messages;

public final class Login extends Message {

    private final String mailAddress;

    public Login(String mailAddress) {
        super(MessageType.LOGIN);
        this.mailAddress = mailAddress;
    }

    public String getMailAddress() {
        return mailAddress;
    }

}
