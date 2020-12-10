package server;

import mailer.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ClientDispatcher implements Runnable {

    private static final String THREAD_NAME = "Client Dispatcher";
    private static final int CORES = Runtime.getRuntime().availableProcessors();

    private final Logger logger;
    private final AtomicBoolean closeRequested;
    private final ServerSocket serverSocket;
    private final ExecutorService exec;

    public ClientDispatcher(Logger logger) throws IOException {
        this.logger = logger;
        closeRequested = new AtomicBoolean(false);
        // If we can't open the server socket, we are done
        serverSocket = new ServerSocket(Constants.SERVER_PORT);
        // Be careful, we initialize exec after serverSocket
        // so that we don't have to shutdown exec if serverSocket new fails
        exec = Executors.newFixedThreadPool(CORES);
    }

    public void close() {
        logger.print("Closing client dispatcher");
        closeRequested.set(true);

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
                if (closeRequested.get()) {
                    break;
                }

                String clientAddress = incoming.getRemoteSocketAddress().toString();
                logger.print(String.format("[%s] - connection accepted", clientAddress));

                // Remember that is the client handler that has to close the socket,
                // even when initialization fails
                try {
                    Runnable task = new ClientHandler(clientAddress, incoming, logger);
                    exec.submit(task);
                } catch (IOClientHandlerException e) {
                    logger.print(String.format("[%s] - cannot open data stream", clientAddress));
                    logger.print(String.format("[%s] - closing connection", clientAddress));
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
