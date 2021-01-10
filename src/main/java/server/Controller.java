package server;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public final class Controller {

    private Model model;

    @FXML
    private ListView<String> logListView;
    @FXML
    private Button closeButton;

    public void initModel(Model model) {
        if (this.model != null) {
            throw new IllegalStateException("Cannot initialize model more than once");
        }

        this.model = model;

        closeButton.setOnAction(__ -> {
            model.close();

            // Disable the close button so that the user cannot press it anymore
            // and trigger the model's close method multiple times
            closeButton.setDisable(true);
        });
        logListView.setItems(model.getLog());

        model.start();
    }

    public void setStage(Stage stage) {
        // Closing the window closes the model
        stage.setOnCloseRequest(__ -> model.close());
    }
}
