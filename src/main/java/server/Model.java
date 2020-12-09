package server;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mailer.Constants;

import java.io.IOException;

public final class Model {

    private final ObservableList<String> log;
    private final Logger logger;
    private final ClientDispatcher clientDispatcher;

    public Model() throws IOException {
        log = FXCollections.observableArrayList();
        logger = new Logger(log);
        // If ClientDispatcher creation fails, we are done
        clientDispatcher = new ClientDispatcher(logger);
    }

    public ObservableList<String> getLog() {
        return log;
    }

    /**
     * Start server
     */
    public void start() {
        logger.print("Starting server - listening on port " + Constants.SERVER_PORT);

        // Start client dispatcher service
        new Thread(clientDispatcher).start();
    }

    /**
     * Close server
     */
    public void close() {
        logger.print("Closing server");

        // Signal client dispatcher to close
        clientDispatcher.close();
    }
}
