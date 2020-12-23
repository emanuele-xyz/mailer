package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
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
        this.model.isLoginButtonDisabledProperty().addListener((__, ___, newVal) -> loginButton.setDisable(newVal));

        loginButton.setOnAction(e -> this.model.tryLogin());

        errorLabel.textProperty().bind(this.model.errorMessageProperty());
    }

    public void setStageAndScene(Stage stage, Scene scene) {
        stage.setOnCloseRequest(e -> model.close());

        model.isLoggedInProperty().addListener((__, ___, newVal) -> {

            if (!newVal) {
                return;
            }

            // Disable stage close event handler
            stage.setOnCloseRequest(null);

            try {
                FXMLLoader mailboxLoader = new FXMLLoader(getClass().getResource("/mailbox.fxml"));
                FXMLLoader viewerLoader = new FXMLLoader(getClass().getResource("/viewer.fxml"));
                FXMLLoader composerLoader = new FXMLLoader(getClass().getResource("/composer.fxml"));

                BorderPane root = new BorderPane();
                root.setLeft(mailboxLoader.load());
                root.setCenter(viewerLoader.load());

                scene.setRoot(root);

                MainModel mainModel = new MainModel(this.model.getServerDispatcher());

                stage.setOnCloseRequest((____) -> mainModel.close());
                stage.setWidth(1200);
                stage.setHeight(800);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            // close model resources
            model.close();
        });
    }
}
