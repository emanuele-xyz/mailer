package client;

import mailer.connections.ConnectionHandler;
import mailer.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

public final class ServerHandler extends ConnectionHandler implements Callable<Message> {

    private final Message message;

    public ServerHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out, Message message) {
        super(socket, in, out);
        this.message = message;
    }

    @Override
    public Message call() {
        Message result = null;
        try {
            result = processMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return result;
    }

    // TODO: should i simplify it? Since all messages are of the form request-response
    private Message processMessage(Message message) throws IOException {
        if (message == null) {
            return null;
        }

        switch (message.getType()) {
            case LOGIN: {
                sendMessage(message);
                return readMessage();
            }

            case FETCH_REQUEST: {
                sendMessage(message);
                return readMessage();
            }

            case ERROR:
            case SUCCESS: {
                return message;
            }
        }

        return null;
    }
}
