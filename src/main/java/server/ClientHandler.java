package server;

import mailer.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public final class ClientHandler implements Runnable {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public ClientHandler(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    @Override
    public void run() {
        try {
            // Wait for Hello message
            Message msg = readMessage();
            if (msg == null) {
                return;
            }
            System.out.println("Received a message from a client");

            // Reply with an Hello message
            if (msg == Message.Hello) {
                out.writeObject(Message.Hello);
            }
            System.out.println("Message is HELLO");
            System.out.println("Sent hello as a response");

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
