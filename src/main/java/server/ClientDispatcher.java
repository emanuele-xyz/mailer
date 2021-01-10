package server;

import mailer.Constants;
import mailer.exceptions.InvalidMailAddressException;
import server.exceptions.MkdirException;
import server.logger.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ClientDispatcher waits for client connections and serves them as they arrive.
 * You must call the close method in order to shutdown this service
 */
public final class ClientDispatcher implements Runnable {

    private static final String THREAD_NAME = "Client Dispatcher";

    /**
     * Max time that the socket waits in MILLISECONDS
     */
    private static final int CLIENT_SOCKET_TIMEOUT = 10 * 1000;

    private final Logger logger;
    private final ServerSocket serverSocket;
    private final MailManager mailManager;
    private final ExecutorService exec;

    public ClientDispatcher(Logger logger) throws IOException, MkdirException, InvalidMailAddressException {
        this.logger = logger;

        // If we can't open the server socket, we are done
        serverSocket = new ServerSocket(Constants.SERVER_PORT);

        // If we can't initialize the mail manager, close the socket
        try {
            mailManager = new MailManager();
        } catch (MkdirException | InvalidMailAddressException e) {
            serverSocket.close();
            throw e;
        }

        // We initialize the service executor last so that we don't have
        // to shut it down if something goes wrong
        exec = Executors.newFixedThreadPool(Constants.CORES);
    }

    /**
     * Close client dispatcher.
     * This is called from another thread
     */
    public void close() {
        logger.print("Closing client dispatcher");

        synchronized (exec) {
            exec.shutdown();
        }

        try {
            // We don't need to synchronize the server socket since close() is thread safe
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

                // If something goes wrong, we close the socket
                // Once the client handler has been submitted, it's its own responsibility to
                // close the socket and data streams
                try {
                    incoming.setSoTimeout(CLIENT_SOCKET_TIMEOUT);
                    ObjectOutputStream out = new ObjectOutputStream(incoming.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(incoming.getInputStream());
                    Runnable task = new ClientHandler(incoming, in, out, clientAddress, mailManager, logger);

                    synchronized (exec) {
                        exec.submit(task);
                    }

                } catch (IOException e) {
                    // Data stream initialization failed, we have to close the socket ourselves
                    // Socket timeout set method failed, we close the socket

                    logger.print("[%s] - error connection setup ... closing socket", clientAddress);

                    try {
                        incoming.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                    e.printStackTrace();
                }

            } catch (SocketException e) {
                // Thrown when the server socket is closed
                // If socket is closed, then the client dispatcher was closed
                // Hence we return, terminating the dispatcher thread
                // This is not a proper error
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
