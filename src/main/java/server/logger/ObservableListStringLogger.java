package server.logger;

import javafx.application.Platform;
import javafx.collections.ObservableList;

public final class ObservableListStringLogger implements Logger {

    private final ObservableList<String> log;

    public ObservableListStringLogger(ObservableList<String> log) {
        this.log = log;
    }

    @Override
    public void print(String message) {
        Platform.runLater(() -> log.add(message));
    }

    @Override
    public void print(String message, Object ... args) {
        print(String.format(message, args));
    }

    public ObservableList<String> getLog() {
        return log;
    }
}
