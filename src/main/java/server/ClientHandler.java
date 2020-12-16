package server;

import mailer.Utils;
import mailer.messages.Error;
import mailer.messages.Login;
import mailer.messages.Message;
import mailer.messages.Success;
import server.exceptions.IOClientHandlerException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public final class ClientHandler implements Runnable {

    private final String address;
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final MailManager mailManager;
    private final Logger logger;

    public ClientHandler(String address, Socket socket, MailManager mailManager, Logger logger) throws IOClientHandlerException {
        this.address = address;
        this.socket = socket;

        // If we cannot open input or output stream we are done!
        // Remember that is our job to close the socket
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {

            try {
                socket.close();
            } catch (IOException ioException) {
                // TODO: what can we do if closing the socket fails?
                ioException.printStackTrace();
            }

            throw new IOClientHandlerException(e.getMessage(), e.getCause());
        }

        this.mailManager = mailManager;

        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            // Wait for client message
            Message msg = Utils.read(Message.class, in);
            if (msg == null) {
                // If we cannot read the message send an error to the client
                // to avoid leaving it hanging
                sendMessage(new Error("Unable to correctly read message"));
                return;
            }

            // Process message
            processMessage(msg);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
        } finally {
            // Close resources

            try {
                in.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

            try {
                out.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

            try {
                socket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

            logger.print("[%s] - connection closed", address);
        }
    }

    private void processMessage(Message message) throws IOException, ClassNotFoundException {
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

    private void processLogin(Login loginMessage) throws IOException {
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

    private void sendMessage(Message message) throws IOException {
        out.writeObject(message);
    }

    private <T> T castMessage(Class<T> target, Message message) {
        if (message != null && message.getClass().equals(target)) {
            return (T) message;
        } else {
            return null;
        }
    }
}
