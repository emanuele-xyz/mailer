package server;

import mailer.Constants;
import mailer.InvalidMailAddressException;
import server.exceptions.MkdirException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ClientDispatcher implements Runnable {

    private static final String THREAD_NAME = "Client Dispatcher";

    private final Logger logger;
    private final ServerSocket serverSocket;
    private final MailManager mailManager;
    private final ExecutorService exec;

    public ClientDispatcher(Logger logger) throws IOException, MkdirException, InvalidMailAddressException {
        this.logger = logger;
        // If we can't open the server socket, we are done
        serverSocket = new ServerSocket(Constants.SERVER_PORT);
        mailManager = new MailManager();
        // Be careful, we initialize exec last so that we don't
        // have to shutdown exec if something goes wrong
        exec = Executors.newFixedThreadPool(Constants.CORES);
    }

    public void close() {
        logger.print("Closing client dispatcher");

        exec.shutdown();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // For debugging purposes
        Thread.currentThread().setName(THREAD_NAME);

        while (true) {
            try {
                // the incoming socket must be closed by its handler
                Socket incoming = serverSocket.accept();

                String clientAddress = incoming.getRemoteSocketAddress().toString();
                logger.print("[%s] - connection accepted", clientAddress);

                // If data stream initialization fails, we will close the socket, otherwise it is
                // responsibility of our handler to close all the resources
                try {
                    ObjectOutputStream out = new ObjectOutputStream(incoming.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(incoming.getInputStream());
                    Runnable task = new ClientHandler(incoming, in, out, clientAddress, mailManager, logger);
                    exec.submit(task);
                } catch (IOException e) {
                    // Data stream initialization failed, we have to close the socket ourselves

                    logger.print("[%s] - cannot open data stream, connection is automatically closed", clientAddress);

                    try {
                        incoming.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                    e.printStackTrace();
                }

            } catch (SocketException e) {
                // Thrown when the server socket is closed.
                // If socket is closed, then the client dispatcher was closed.
                // Hence we return, terminating the dispatcher thread.
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
