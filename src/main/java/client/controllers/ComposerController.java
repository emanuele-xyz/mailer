package client.controllers;

import client.MainModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
    private Button clear;

    @FXML
    private Button send;

    public void initModel(MainModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Cannot initialize model more than once");
        }

        this.model = model;

        model.getMailDraft().subjectProperty().bindBidirectional(subject.textProperty());
        subject.disableProperty().bind(model.isSendingProperty());

        model.getMailDraft().getTos().addListener((ListChangeListener<SimpleStringProperty>) change -> {
            if (!change.next()) {
                // this is the last change
                return;
            }

            if (change.wasRemoved()) {
                return;
            }

            for (SimpleStringProperty added : change.getAddedSubList()) {
                TextField textField = new TextField();
                added.bind(textField.textProperty());
                textField.disableProperty().bind(model.isSendingProperty());
                to.getChildren().add(textField);

                textField.focusedProperty().addListener((__, ___, isFocused) -> {
                    if (!isFocused && textField.getText().isEmpty()) {
                        model.getMailDraft().removeRecipient(added);
                        added.unbind();
                        textField.disableProperty().unbind();
                        to.getChildren().remove(textField);
                    }
                });
            }
        });

        addRecipient.setOnAction(__ -> model.getMailDraft().addRecipient());
        addRecipient.disableProperty().bind(model.isSendingProperty());

        model.getMailDraft().textProperty().bindBidirectional(text.textProperty());
        text.disableProperty().bind(model.isSendingProperty());

        model.getMailDraft().clearProperty().addListener((__, ___, mustClear) -> {
            if (mustClear) {
                to.getChildren().clear();
            }
        });
        clear.setOnAction(__ -> model.clearDraft());
        clear.disableProperty().bind(model.isSendingProperty());

        send.setOnAction(__ -> model.sendMail());
        send.disableProperty().bind(model.isSendingProperty());
    }
}
