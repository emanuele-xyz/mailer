package mailer.messages;

/**
 * Message meaning that everything went accordingly
 */
public final class SuccessMessage extends Message {

    public SuccessMessage() {
        super(MessageType.SUCCESS);
    }
}
