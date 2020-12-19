package client;

import javafx.stage.Stage;

public final class MainController {

    private MainModel model;

    public void initModel(MainModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Login model can only be initialized once");
        }

        this.model = model;
    }

    public void setStage(Stage stage) {
        stage.setOnCloseRequest(e -> model.close());
    }
}
