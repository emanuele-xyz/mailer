package client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public final class MailboxController {

    private MainModel model;

    @FXML
    private Label username;

    @FXML
    private Button newMail;

    @FXML
    private ListView<String> mails;

    public void initModel(MainModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Cannot initialize model more than once");
        }

        this.model = model;

        username.setText(model.getUser());
    }
}
