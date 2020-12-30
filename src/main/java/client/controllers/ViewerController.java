package client.controllers;

import client.MainModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public final class ViewerController {

    private MainModel model;

    @FXML
    private Label subject;

    @FXML
    private Label from;

    @FXML
    private Label to;

    @FXML
    private Label date;

    @FXML
    private TextArea text;

    @FXML
    private Button reply;

    @FXML
    private Button replyAll;

    @FXML
    private Button forward;

    public void initModel(MainModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Cannot initialize model more than once");
        }

        this.model = model;

        subject.textProperty().bind(model.getSelectedMail().subjectProperty());
        from.textProperty().bind(model.getSelectedMail().fromProperty());
        to.textProperty().bind(model.getSelectedMail().toProperty());
        date.textProperty().bind(model.getSelectedMail().dateProperty());

        text.textProperty().bind(model.getSelectedMail().textProperty());
        text.setEditable(false);

        reply.setOnAction(__ -> model.reply());
        reply.disableProperty().bind(model.isSendingProperty());

        replyAll.setOnAction(__ -> model.replyAll());
        replyAll.disableProperty().bind(model.isSendingProperty());

        forward.setOnAction(__ -> model.forward());
        forward.disableProperty().bind(model.isSendingProperty());
    }
}
