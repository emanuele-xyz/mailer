package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public final class LoginController {

    private LoginModel model;

    @FXML
    private TextField usernameTextField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    public void initModel(LoginModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Login model can only be initialized once");
        }

        this.model = model;
        this.model.usernameProperty().bind(usernameTextField.textProperty());

        loginButton.setOnAction(e -> this.model.tryLogin());

        errorLabel.textProperty().bind(this.model.errorMessageProperty());
    }

    public void setStageAndScene(Stage stage, Scene scene) {
        stage.setOnCloseRequest(e -> model.close());

        model.isLoggedInProperty().addListener(e -> {
            // Disable stage close event handler
            stage.setOnCloseRequest(null);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/client.fxml"));
                scene.setRoot(loader.load());

                MainController mainController = loader.getController();
                MainModel mainModel = new MainModel(this.model.getServerDispatcher());
                mainController.initModel(mainModel);
                mainController.setStage(stage);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }
}
