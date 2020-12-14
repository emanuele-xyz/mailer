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

            out.writeObject(Message.Login);
            out.writeObject("marco@mailer.xyz");

            System.out.println("Sent LOGIN message to server");

            try {
                Object tmp = in.readObject();
                if (tmp != null && tmp.getClass().equals(Boolean.class)) {
                    Boolean response = (Boolean) tmp;
                    if (response) {
                        System.out.println("Identified!");
                    } else {
                        System.out.println("Not identified!");
                    }
                }
            } catch (ClassNotFoundException e) {
                System.err.println(e.getMessage());
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
