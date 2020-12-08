package server;

import javafx.application.Platform;
import javafx.collections.ObservableList;
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
    private final ObservableList<String> log;

    public ClientHandler(String address, Socket socket, ObjectOutputStream out, ObjectInputStream in, ObservableList<String> log) {
        this.address = address;
        this.socket = socket;
        this.out = out;
        this.in = in;
        this.log = log;
    }

    @Override
    public void run() {
        try {
            // Wait for Hello message
            Message msg = readMessage();
            if (msg == null) {
                return;
            }

            Thread.sleep(1000000000);

            // Reply with an Hello message
            if (msg == Message.Hello) {
                Platform.runLater(() -> log.add(String.format("[%s] - received HELLO message", address)));
                out.writeObject(Message.Hello);
                Platform.runLater(() -> log.add(String.format("[%s] - sent HELLO message in response", address)));
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
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
