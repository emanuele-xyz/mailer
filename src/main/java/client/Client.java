package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

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

            FXMLLoader errorLoader = new FXMLLoader(getClass().getResource("/error.fxml"));
            Parent error = errorLoader.load();
            ErrorController errorController = errorLoader.getController();

            BorderPane root = new BorderPane();
            root.setLeft(mailbox);
            root.setCenter(viewer);
            root.setBottom(error);

            MainModel mainModel = new MainModel(user);
            mailboxController.initModel(mainModel);
            viewerController.initModel(mainModel);
            composerController.initModel(mainModel);
            errorController.initModel(mainModel);

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
