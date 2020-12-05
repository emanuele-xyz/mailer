package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Server extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlURL = getClass().getResource("/server.fxml");
        Parent root = FXMLLoader.load(fxmlURL);
        stage.setTitle("First App!");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
