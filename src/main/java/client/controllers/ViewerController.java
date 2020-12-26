package client.controllers;

import client.MainModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;

public final class ViewerController {

    private MainModel model;

    @FXML
    private Label subject;

    @FXML
    private Label from;

    @FXML
    private FlowPane to;

    @FXML
    private Label date;

    @FXML
    private TextArea text;

    public void initModel(MainModel model) {

    }

}
