package server;

import mailer.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public final class ClientHandler implements Runnable {

    private final String address;
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final Logger logger;

    public ClientHandler(String address, Socket socket, Logger logger) throws IOClientHandlerException {
        this.address = address;
        this.socket = socket;

        // If we cannot open input or output stream we are done!
        // Remember that we have to close the socket
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

        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            // Wait for Hello message
            Message msg = readMessage();
            if (msg == null) {
                return;
            }

            // Reply with an Hello message
            if (msg == Message.Hello) {
                logger.print(String.format("[%s] - received HELLO message", address));
                out.writeObject(Message.Hello);
                logger.print(String.format("[%s] - sent HELLO message in response", address));
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (ClassNotFoundException e) {
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
        }
    }

    private Message readMessage() throws IOException, ClassNotFoundException {
        Object tmp = in.readObject();
        Message msg = null;
        if (tmp != null && tmp.getClass().equals(Message.class)) {
            msg = (Message) tmp;
        }

        return msg;
    }
}
