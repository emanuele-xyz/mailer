package mailer.messages;

import java.io.Serializable;

/**
 * Message is the base class for all messages
 */
public abstract class Message implements Serializable {

    private final MessageType type;

    public Message(MessageType type) {
        this.type = type;
    }

    /**
     * Get the message type
     * @return the message type
     */
    public MessageType getType() {
        return type;
    }
}
