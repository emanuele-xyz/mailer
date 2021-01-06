package client.controllers;

import client.models.MainModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class PopupController {

    private MainModel model;

    private Stage popupStage;

    @FXML
    private Label number;

    @FXML
    private Button ok;

    public void initModel(MainModel model, Parent popup, Stage stage) {
        if (this.model != null) {
            throw new IllegalStateException("Cannot initialize model more than once");
        }

        this.model = model;

        initPopupStage(popup, stage);

        model.newMailsReceivedProperty().addListener((__, ___, newMails) -> {
            // If there are no new mails, don't show popup
            if (newMails.equals(0)) {
                return;
            }

            // If there is already a popup on the screen
            if (popupStage.isShowing()) {
                // Hide popup
                popupStage.hide();
                // Add previous mail number to newMails
                newMails = ((Integer) newMails) + Integer.parseInt(number.getText());
            }

            number.setText(newMails.toString());
            popupStage.show();
        });

        ok.setOnAction(__ -> popupStage.hide());
    }

    private void initPopupStage(Parent popup, Stage stage) {
        popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(stage);
        Scene scene = new Scene(popup);
        popupStage.setScene(scene);
    }
}
