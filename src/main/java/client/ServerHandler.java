package client;

import mailer.connections.ConnectionHandler;
import mailer.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public final class ServerHandler extends ConnectionHandler {

    private final Message message;

    public ServerHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out, Message message) {
        super(socket, in, out);
        this.message = message;
    }

    @Override
    public void execute() throws IOException, ClassNotFoundException {
        // TODO: dispatch message

        switch (message.getType()) {
            case LOGIN:
                // TODO: send login message
                break;

            case ERROR:
                // TODO: send error message
                break;

            case SUCCESS:
                // TODO: send success message
                break;
        }
    }
}
