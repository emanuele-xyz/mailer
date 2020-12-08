package server;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mailer.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Model {

    private static final int CORES = Runtime.getRuntime().availableProcessors();

    private final ServerSocket socket;

    private final AtomicBoolean stopped;
    private final ObservableList<String> log;

    public Model() throws IOException {
        socket = new ServerSocket(Constants.SERVER_PORT);
        stopped = new AtomicBoolean(true);
        // TODO: should I handle synchronization myself?
        log = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    }

    public ObservableList<String> getLog() {
        return log;
    }

    /**
     * Start server
     */
    public void start() {
        if (!stopped.get()) {
            return;
        }

        stopped.set(false);

        log.add("Starting server ... - listening on port " + Constants.SERVER_PORT);
        ExecutorService exec = Executors.newFixedThreadPool(CORES);
        exec.submit(() -> dispatchClientConnections(exec));
    }

    /**
     * Stop server
     */
    public void stop() {
        if (stopped.get()) {
            return;
        }

        log.add("Server Stopped");
        stopped.set(true);
    }

    public void close() {
        stopped.set(true);

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispatchClientConnections(ExecutorService exec) {
        while (!stopped.get()) {
            try {
                // Socket, input and output streams must be closed by their handlers
                Socket incoming = socket.accept();
                ObjectOutputStream out = new ObjectOutputStream(incoming.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(incoming.getInputStream());

                // Log client connection
                String clientAddress = incoming.getRemoteSocketAddress().toString();
                String logMessage = String.format("Client %s - connection accepted", clientAddress);
                log.add(logMessage);

                Runnable task = new ClientHandler(clientAddress, incoming, out, in, log);
                exec.submit(task);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
