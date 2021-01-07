package mailer.messages;

/**
 * Message meaning that something went wrong
 */
public final class ErrorMessage extends Message {

    private final String message;

    public ErrorMessage(String message) {
        super(MessageType.ERROR);
        this.message = message;
    }

    /**
     * @return the error message
     */
    public String getMessage() {
        return message;
    }
}
