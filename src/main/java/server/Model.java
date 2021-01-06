package server;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mailer.Constants;
import mailer.exceptions.InvalidMailAddressException;
import server.exceptions.MkdirException;
import server.logger.ObservableListStringLogger;

import java.io.IOException;

/**
 * Model for mailer server
 */
public final class Model {

    private final ObservableListStringLogger logger;
    private final ClientDispatcher clientDispatcher;

    public Model() throws IOException, MkdirException, InvalidMailAddressException {
        logger = new ObservableListStringLogger(FXCollections.observableArrayList());
        // If ClientDispatcher creation fails, we are done
        clientDispatcher = new ClientDispatcher(logger);
    }

    public ObservableList<String> getLog() {
        return logger.getLog();
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
