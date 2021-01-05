package mailer.connections;

import mailer.Utils;
import mailer.messages.LoginMessage;
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

    // We check the sendMessage return value only in client code.
    // Why is that? Because in server code, what can we do if sendMessage fails?
    // We could try to resend the message, but what if it fails again?
    // For this reason, in server code we try to send the message and if the send fails
    // the client will timeout and retry later!
    // In client code, however, we check for the return value, why?
    // Because if we fail to send a message, it's useless to wait for a message that will never
    // come, even if the connection will timeout. We stop what we were doing immediately
    protected final boolean sendMessage(Message message) {
        boolean success = true;

        try {
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    protected final <T> T castMessage(Class<T> target, Message message) {
        T tmp =  Utils.tryCast(target, message);

        // If it's null there is a mismatch between message type and class. This is a bug.
        // Fix it in the appropriate message class!
        assert tmp != null;

        return tmp;
    }
}
