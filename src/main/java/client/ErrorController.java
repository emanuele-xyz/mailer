package client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class ErrorController {

    private MainModel model;

    @FXML
    private Label errorMessage;

    public void initModel(MainModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Cannot initialize model more than once");
        }

        this.model = model;

        errorMessage.textProperty().bind(model.errorMessageProperty());
    }
}
