package client;

import client.controllers.ComposerController;
import client.controllers.MessageController;
import client.controllers.MailboxController;
import client.controllers.ViewerController;
import client.login.LoginController;
import client.login.LoginModel;
import client.login.LoginResult;
import client.model.MainModel;
import client.model.MainModelStateProperty;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import mailer.InvalidMailAddressException;
import mailer.MailAddress;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

public final class Client extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        LoginResult loginResult = showLoginAndWait();
        if (loginResult.isSuccessful()) {
            showMainScreen(stage, loginResult.getAccount());
        }
    }

    private LoginResult showLoginAndWait() throws IOException {
        try {
            Stage stage = new Stage();

            URL fxmlURL = getClass().getResource("/login.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlURL);
            Parent root = loader.load();
            LoginController controller = loader.getController();

            stage.setTitle("Mailer client login");
            stage.setResizable(false);
            Scene scene = new Scene(root);
            stage.setScene(scene);

            LoginModel model = new LoginModel();
            controller.initModel(model);
            controller.initStage(stage);

            stage.showAndWait();

            return new LoginResult(model.isLoggedIn(), model.getUsername());

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return new LoginResult(false, "");
    }

    private void showMainScreen(Stage stage, String user) throws IOException {
        try {
            FXMLLoader mailboxLoader = new FXMLLoader(getClass().getResource("/mailbox.fxml"));
            Parent mailbox = mailboxLoader.load();
            MailboxController mailboxController = mailboxLoader.getController();

            FXMLLoader viewerLoader = new FXMLLoader(getClass().getResource("/viewer.fxml"));
            Parent viewer = viewerLoader.load();
            ViewerController viewerController = viewerLoader.getController();

            FXMLLoader composerLoader = new FXMLLoader(getClass().getResource("/composer.fxml"));
            Parent composer = composerLoader.load();
            ComposerController composerController = composerLoader.getController();

            FXMLLoader errorLoader = new FXMLLoader(getClass().getResource("/message.fxml"));
            Parent error = errorLoader.load();
            MessageController messageController = errorLoader.getController();

            FXMLLoader blankLoader = new FXMLLoader(getClass().getResource("/blank.fxml"));
            Parent blank = blankLoader.load();

            BorderPane root = new BorderPane();
            root.setLeft(mailbox);
            root.setCenter(blank);
            root.setBottom(error);

            MailAddress userAddress = null;
            try {
                userAddress = new MailAddress(user);
            } catch (InvalidMailAddressException e) {
                // Passing an invalid user should not happen.
                // If it does, it's a programmer error
                assert false;
            }
            MainModel mainModel = new MainModel(userAddress);
            mailboxController.initModel(mainModel);
            viewerController.initModel(mainModel);
            composerController.initModel(mainModel);
            messageController.initModel(mainModel);

            // Set main model state transitions after all controllers have been initialized
            mainModel.getCurrentState().stateIndexProperty().addListener((__, oldVal, newVal) -> {
                if (oldVal.equals(newVal)) {
                    // the state is the same
                    return;
                }

                if (newVal.equals(MainModelStateProperty.BLANK)) {
                    // Transition to blank state
                    root.setCenter(blank);
                } else if (newVal.equals(MainModelStateProperty.VIEWING)) {
                    // Transition to viewing state
                    root.setCenter(viewer);
                } else if (newVal.equals(MainModelStateProperty.COMPOSING)) {
                    // Transition to composing state
                    root.setCenter(composer);
                } else {
                    // This should never happen.
                    // If it does, it's a programmer error!
                    assert false;
                }
            });

            stage.setOnCloseRequest((____) -> mainModel.close());
            stage.setTitle("Mailer client");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
