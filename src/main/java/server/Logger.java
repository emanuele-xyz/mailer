package server;

import javafx.application.Platform;
import javafx.collections.ObservableList;

public final class Logger {

    private final ObservableList<String> log;

    public Logger(ObservableList<String> log) {
        this.log = log;
    }

    public void print(String message) {
        Platform.runLater(() -> log.add(message));
    }
}
