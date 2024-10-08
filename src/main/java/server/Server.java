package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mailer.exceptions.InvalidMailAddressException;
import server.exceptions.MkdirException;

import java.io.IOException;
import java.net.URL;

public final class Server extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            URL fxmlURL = getClass().getResource("/server.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlURL);
            Parent root = loader.load();
            Controller controller = loader.getController();

            Model model = new Model();
            controller.initModel(model);
            controller.setStage(stage);

            stage.setTitle("Mailer server");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (MkdirException | InvalidMailAddressException e) {
            System.err.printf("Error initializing server: '%s'", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
