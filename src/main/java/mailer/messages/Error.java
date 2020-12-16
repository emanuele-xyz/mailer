package mailer.messages;

public final class Error extends Message {

    private final String message;

    public Error(String message) {
        super(MessageType.ERROR);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
