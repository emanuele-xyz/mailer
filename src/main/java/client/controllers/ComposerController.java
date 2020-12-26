package client.controllers;

import client.MainModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public final class ComposerController {

    private MainModel model;

    @FXML
    private TextField subject;

    @FXML
    private FlowPane to;

    @FXML
    private Button addRecipient;

    @FXML
    private TextArea text;

    @FXML
    private Button send;

    public void initModel(MainModel model) {

    }

}
