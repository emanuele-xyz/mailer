package client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    public void initStage(Stage stage) {
        stage.setOnHidden(__ -> model.close());

        model.isLoggedInProperty().addListener((__, ___, newVal) -> {
            if (!newVal) {
                return;
            }
            stage.hide();
        });
    }
}
