package client.controllers;

import client.models.MainModel;
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
    private Button deleteMail;

    @FXML
    private ListView<Mail> mails;

    public void initModel(MainModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Cannot initialize model more than once");
        }

        this.model = model;

        username.setText(model.getUser());

        newMail.setOnAction(__ -> model.getCurrentState().setComposing());

        deleteMail.setOnAction(__ -> model.deleteMail());
        deleteMail.disableProperty().bind(model.isDeletingProperty());

        mails.setItems(model.getMails());
        mails.getSelectionModel().selectedItemProperty().addListener((__, ___, newSel) -> {
            if (newSel == null) {
                return;
            }

            model.getCurrentState().setViewing();
            model.getSelectedMail().select(newSel);
        });
        mails.focusedProperty().addListener((__, ___, isFocused) -> {
            if (isFocused) {
                // If mail list was not focused, if we click on the item we selected
                // the list view selectedItemProperty change listener won't be triggered.
                // To fix this, we force the change listener to be triggered by programmatically
                // re-selecting the item that was previously selected
                Mail selectedMail = mails.getSelectionModel().getSelectedItem();
                mails.getSelectionModel().select(null);
                mails.getSelectionModel().select(selectedMail);
            }
        });
    }
}
