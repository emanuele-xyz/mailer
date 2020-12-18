package mailer.messages;

public final class ErrorMessage extends Message {

    private final String message;

    public ErrorMessage(String message) {
        super(MessageType.ERROR);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
