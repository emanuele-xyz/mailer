package client.controllers;

import client.model.MainModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class MessageController {

    private MainModel model;

    @FXML
    private Label message;

    public void initModel(MainModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Cannot initialize model more than once");
        }

        this.model = model;

        message.textProperty().bind(model.errorMessageProperty());
    }
}
