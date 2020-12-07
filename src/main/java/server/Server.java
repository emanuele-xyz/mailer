package server;

import mailer.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Server {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT)) {
            int cores = Runtime.getRuntime().availableProcessors();
            ExecutorService exec = Executors.newFixedThreadPool(cores);
            dispatchClients(serverSocket, exec);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void dispatchClients(ServerSocket serverSocket, ExecutorService exec) {
        // TODO: right now only a client is processed
        // TODO: remember to shutdown the executor service, otherwise the server will hang
        // while (true) {
            try {
                // Socket, input and output streams must be closed by their handlers
                Socket incoming = serverSocket.accept();
                ObjectOutputStream out = new ObjectOutputStream(incoming.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(incoming.getInputStream());

                System.out.println("Connected to a client");

                Runnable task = new ClientHandler(incoming, out, in);
                exec.submit(task);
                exec.shutdown();

            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        // }
    }
}
