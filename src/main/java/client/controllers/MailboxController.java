package client.controllers;

import client.MainModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import mailer.Mail;

public final class MailboxController {

    private MainModel model;

    @FXML
    private Label username;

    @FXML
    private Button newMail;

    @FXML
    private ListView<Mail> mails;

    public void initModel(MainModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Cannot initialize model more than once");
        }

        this.model = model;

        username.setText(model.getUser());

        mails.setItems(model.getMails());
        mails.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            model.getCurrentState().setViewing();
            model.getSelectedMail().select(newSel);
        });

        newMail.setOnAction(e -> model.getCurrentState().setComposing());
    }
}
