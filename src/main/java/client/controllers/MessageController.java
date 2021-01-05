package client.controllers;

import client.model.MainModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class MessageController {

    private MainModel model;

    @FXML
    private Label success;

    @FXML
    private Label error;

    public void initModel(MainModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Cannot initialize model more than once");
        }

        this.model = model;

        success.textProperty().bind(model.successMessageProperty());
        error.textProperty().bind(model.errorMessageProperty());
    }
}
