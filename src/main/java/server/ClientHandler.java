package server;

import mailer.Message;
import mailer.Utils;
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
        switch (message) {
            case Hello: {
                logger.print("[%s] - received HELLO message", address);
                out.writeObject(Message.Hello);
                logger.print("[%s] - sent HELLO message in response", address);
                break;
            }
            case Login: {
                logger.print("[%s] - received LOGIN message", address);

                // Wait for mail address and verify it
                String mailAddress = Utils.read(String.class, in);
                boolean result = mailManager.verify(mailAddress);

                logger.print("[%s] - user is %s registered", address, result ? "" : "not");
                out.writeObject(result);
                break;
            }
        }
    }
}
