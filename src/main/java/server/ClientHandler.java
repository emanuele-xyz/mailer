package server;

import mailer.connections.ConnectionHandler;
import mailer.messages.Error;
import mailer.messages.Login;
import mailer.messages.Message;
import mailer.messages.Success;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public final class ClientHandler extends ConnectionHandler implements Runnable {

    private final String address;
    private final MailManager mailManager;
    private final Logger logger;

    public ClientHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out, String address, MailManager mailManager, Logger logger) {
        super(socket, in, out);
        this.address = address;
        this.mailManager = mailManager;
        this.logger = logger;
    }

    @Override
    public void run() {
        // Wait for client message
        Message msg = readMessage();
        if (msg == null) {
            // If we cannot read the message send an error to the client
            // to avoid leaving it hanging
            // TODO: always check send message return value
            sendMessage(new Error("Unable to correctly read message"));
            logger.print("[%s] - error reading message");
            return;
        }

        // Process message
        // TODO: return boolean flag to signify failure
        processMessage(msg);

        closeConnection();
        logger.print("[%s] - closing connection", address);
  }

    private void processMessage(Message message) {
        switch (message.getType()) {
            case LOGIN: {
                Login login = castMessage(Login.class, message);
                if (login == null) {
                    sendMessage(new Error("Cannot interpret message as login message"));
                } else {
                    processLogin(login);
                }
            }

            case ERROR:
            case SUCCESS:
                // If server receives a success or error message
                // without any context it doesn't do anything
                break;
        }
    }

    private void processLogin(Login loginMessage) {
        logger.print("[%s] - received %s message", address, loginMessage.getType());

        // Verify mail address and log result
        boolean result = mailManager.verify(loginMessage.getMailAddress());
        logger.print("[%s] - user is %s registered", address, result ? "" : "not");

        if (result) {
            sendMessage(new Success());
        } else {
            sendMessage(new Error("Unknown mail"));
        }
    }
}
