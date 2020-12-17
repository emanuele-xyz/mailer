package mailer.connections;

import mailer.Utils;
import mailer.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class ConnectionHandler {

    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public ConnectionHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    public final void closeConnection() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected final Message readMessage() {
        return Utils.read(Message.class, in);
    }

    protected final void sendMessage(Message message) throws IOException {
        out.writeObject(message);
    }

    protected final <T> T castMessage(Class<T> target, Message message) {
        if (message != null && message.getClass().equals(target)) {
            return (T) message;
        } else {
            return null;
        }
    }
}
