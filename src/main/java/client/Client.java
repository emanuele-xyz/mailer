package client;

import mailer.Constants;
import mailer.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public final class Client {

    public static void main(String[] args) {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            connectToServer(hostname);
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void connectToServer(String hostname) {
        try (Socket s = new Socket(hostname, Constants.SERVER_PORT);
             ObjectInputStream in = new ObjectInputStream(s.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream())) {

            System.out.println("Connected to server");

            out.writeObject(Message.Hello);

            System.out.println("Sent HELLO message to server");

            try {
                Object tmp = in.readObject();
                if (tmp != null && tmp.getClass().equals(Message.class)) {
                    Message response = (Message) tmp;
                    if (response == Message.Hello) {
                        System.out.println("Received HELLO message from server");
                    }
                }
            } catch (ClassNotFoundException e) {
                System.err.println(e.getMessage());
            }

        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
