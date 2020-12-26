package client;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

public final class Logger {

    private final SimpleStringProperty errorLabel;

    public Logger(SimpleStringProperty errorLabel) {
        this.errorLabel = errorLabel;
    }

    public void print(String message) {
        Platform.runLater(() -> errorLabel.set(message));
    }

    public void print(String message, Object ... args) {
        Platform.runLater(() -> errorLabel.set(String.format(message, args)));
    }
}
