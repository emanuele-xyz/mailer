package server;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public final class Controller {

    private Model model;

    @FXML
    private ListView<String> logListView;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;

    public void initModel(Model model) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }

        this.model = model;

        logListView.setItems(model.getLog());

        startButton.setOnAction(e -> model.start());

        stopButton.setOnAction(e -> model.stop());
    }

}
