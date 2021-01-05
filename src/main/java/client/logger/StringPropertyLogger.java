package client.logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

public final class StringPropertyLogger implements Logger {

    private final SimpleStringProperty successMessage;
    private final SimpleStringProperty errorMessage;

    public StringPropertyLogger(SimpleStringProperty successMessage, SimpleStringProperty errorMessage) {
        this.successMessage = successMessage;
        this.errorMessage = errorMessage;
    }

    @Override
    public void success(String message) {
        Platform.runLater(() -> {
            errorMessage.set("");
            successMessage.set(message);
        });
    }

    @Override
    public void success(String message, Object... args) {
        success(String.format(message, args));
    }

    @Override
    public void error(String message) {
        Platform.runLater(() -> {
            successMessage.set("");
            errorMessage.set(message);
        });
    }

    @Override
    public void error(String message, Object ... args) {
        error(String.format(message, args));
    }


    public SimpleStringProperty successMessageProperty() {
        return successMessage;
    }

    public SimpleStringProperty errorMessageProperty() {
        return errorMessage;
    }
}
