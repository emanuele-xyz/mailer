package client.logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDateTime;

/**
 * Logger that uses a <code>SimpleStringProperty</code> as output
 */
public final class StringPropertyLogger implements Logger {

    private final SimpleStringProperty successMessage;
    private final SimpleStringProperty errorMessage;

    public StringPropertyLogger() {
        this.successMessage = new SimpleStringProperty();
        this.errorMessage = new SimpleStringProperty();
    }

    @Override
    public void success(String message) {
        String time = getTimeString();
        Platform.runLater(() -> {
            errorMessage.set("");
            successMessage.set(String.format("[%s] - %s", time, message));
        });
    }

    @Override
    public void error(String message) {
        String time = getTimeString();
        Platform.runLater(() -> {
            successMessage.set("");
            errorMessage.set(String.format("[%s] - %s", time, message));
        });
    }

    @Override
    public void success(String message, Object... args) {
        success(String.format(message, args));
    }

    @Override
    public void error(String message, Object... args) {
        error(String.format(message, args));
    }


    public SimpleStringProperty successMessageProperty() {
        return successMessage;
    }

    public SimpleStringProperty errorMessageProperty() {
        return errorMessage;
    }

    private static String getTimeString() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int min = now.getMinute();
        int sec = now.getSecond();
        return String.format("%02d:%02d:%02d", hour, min, sec);
    }
}
