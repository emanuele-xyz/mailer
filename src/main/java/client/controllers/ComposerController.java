package client.controllers;

import client.MainModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
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
        if (this.model != null) {
            throw new IllegalStateException("Cannot initialize model more than once");
        }

        this.model = model;

        model.getMailDraft().subjectProperty().bind(subject.textProperty());

        addRecipient.setOnAction(__ -> {
            SimpleStringProperty recipient = model.getMailDraft().addRecipient();
            TextField textField = new TextField();
            recipient.bind(textField.textProperty());
            to.getChildren().add(textField);

            textField.focusedProperty().addListener((___, ____, isFocused) -> {
                if (!isFocused && textField.getText().isEmpty()) {
                    // Focusing out from text field and text field is empty
                    // No recipient was added, delete this recipient and remove
                    // text field from flow pane
                    model.getMailDraft().removeRecipient(recipient);
                    to.getChildren().remove(textField);
                }
            });
        });

        model.getMailDraft().textProperty().bind(text.textProperty());
    }
}
