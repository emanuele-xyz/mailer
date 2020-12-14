package server;

import mailer.Constants;
import mailer.InvalidMailAddressException;
import server.exceptions.IOClientHandlerException;
import server.exceptions.MkdirException;

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
    private final MailManager mailManager;
    private final ExecutorService exec;

    public ClientDispatcher(Logger logger) throws IOException, MkdirException, InvalidMailAddressException {
        this.logger = logger;
        closeRequested = new AtomicBoolean(false);
        // If we can't open the server socket, we are done
        serverSocket = new ServerSocket(Constants.SERVER_PORT);
        mailManager = new MailManager();
        // Be careful, we initialize exec last so that we don't
        // have to shutdown exec if something goes wrong
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
                logger.print("[%s] - connection accepted", clientAddress);

                // Remember that is the client handler that has to close the socket,
                // even when initialization fails
                try {
                    Runnable task = new ClientHandler(clientAddress, incoming, mailManager, logger);
                    exec.submit(task);
                } catch (IOClientHandlerException e) {
                    logger.print("[%s] - cannot open data stream", clientAddress);
                    logger.print("[%s] - closing connection", clientAddress);
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
