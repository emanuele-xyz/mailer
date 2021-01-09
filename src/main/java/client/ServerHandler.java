package client;

import mailer.connections.ConnectionHandler;
import mailer.messages.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * ServerHandler manages sending a message and retrieving a response from the server.
 * It is spawned by <code>ServerDispatcher</code>
 */
public final class ServerHandler extends ConnectionHandler implements Callable<Message> {

    private final Message message;

    public ServerHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out, Message message) {
        super(socket, in, out);
        this.message = message;
    }

    @Override
    public Message call() {
        Message result = processMessage(message);
        closeConnection();
        return result;
    }

    /**
     * Sends a message to the server and returns a response
     * @param message the message to be sent
     * @return server response message
     */
    private Message processMessage(Message message) {
        if (message == null) {
            return null;
        }

        boolean sendSuccessful = sendMessage(message);
        if (!sendSuccessful) {
            // We don't have to wait for a message that will never
            // come since sendMessage failed
            System.err.println("'sendMessage' method failed");
            return null;
        } else {
            return readMessage();
        }
    }
}
