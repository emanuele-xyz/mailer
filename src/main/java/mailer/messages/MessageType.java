package mailer.messages;

/**
 * MessageType mirrors all the available concrete classes of messages.
 * It is used to allow switch statements to implement logic for different
 * messages
 */
public enum MessageType {
    LOGIN,
    FETCH_REQUEST,
    FETCH_RESPONSE,
    MAIL_PUSH,
    MAIL_DELETE,
    ERROR,
    SUCCESS,
}
