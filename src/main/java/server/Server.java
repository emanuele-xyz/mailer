package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public final class Server extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlURL = getClass().getResource("/server.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlURL);
        Parent root = loader.load();
        Controller controller = loader.getController();

        Model model = new Model();
        controller.initModel(model);

        stage.setTitle("First App!");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
