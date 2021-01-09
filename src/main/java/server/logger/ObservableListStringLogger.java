package server.logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Logger that uses an ObservableList of String as output
 */
public final class ObservableListStringLogger implements Logger {

    private final ObservableList<String> log;

    public ObservableListStringLogger() {
        this.log = FXCollections.observableArrayList();
    }

    @Override
    public void print(String message) {
        Platform.runLater(() -> log.add(message));
    }

    @Override
    public void print(String format, Object ... args) {
        print(String.format(format, args));
    }

    public ObservableList<String> getLog() {
        return log;
    }
}
