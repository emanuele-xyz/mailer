package mailer.connections;

import mailer.Utils;
import mailer.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * ConnectionHandler manages some kind of connection consisting of a socket and its data streams
 */
public abstract class ConnectionHandler {

    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public ConnectionHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    /**
     * Close the connection
     */
    public final void closeConnection() {
        try {
            // Closing the socket will also close it's input and output streams
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read a message from socket input stream
     *
     * @return A message if the read was successful, null otherwise
     */
    protected final Message readMessage() {
        return Utils.read(Message.class, in);
    }

    /**
     * Send a message into socket output stream
     *
     * @param message the message to be sent
     * @return true if the send was successful, false otherwise
     */
    protected final boolean sendMessage(Message message) {
        // We check the sendMessage return value only in client code.
        // Why is that? Because in server code, what can we do if sendMessage fails?
        // We could try to resend the message, but what if it fails again?
        // For this reason, in server code we try to send the message and if the send fails
        // the client will timeout and retry later!
        // In client code, however, we check for the return value, why?
        // Because if we fail to send a message, it's useless to wait for a message that will never
        // come, even if the connection will timeout. We stop what we were doing immediately

        boolean success = true;

        try {
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    /**
     * Cast a message to the specified target message subtype
     *
     * @param target  target message class
     * @param message the message to be cast
     * @return the cast message
     */
    protected final <T extends Message> T castMessage(Class<T> target, Message message) {
        T tmp = Utils.tryCast(target, message);

        // If it's null, it's reasonable to assume that there is a mismatch between message type and class.
        // This is a bug.
        // Fix it in the appropriate message class!
        assert tmp != null;

        return tmp;
    }
}
