package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

public final class Client extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            URL fxmlURL = getClass().getResource("/login.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlURL);
            Parent root = loader.load();
            LoginController controller = loader.getController();

            stage.setTitle("Mailer client");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setAlwaysOnTop(true);

            LoginModel model = new LoginModel();
            controller.initModel(model);
            controller.setStageAndScene(stage, scene);

            stage.show();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) {
//        try {
//            String hostname = InetAddress.getLocalHost().getHostName();
//            connectToServer(hostname);
//        } catch (UnknownHostException e) {
//            System.err.println(e.getMessage());
//        }
//    }
//
//    private static void connectToServer(String hostname) {
//        try (Socket s = new Socket(hostname, Constants.SERVER_PORT);
//             ObjectInputStream in = new ObjectInputStream(s.getInputStream());
//             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream())) {
//
//            System.out.println("Connected to server");
//
//            out.writeObject(Message.Login);
//            out.writeObject("marco@mailer.xyz");
//
//            System.out.println("Sent LOGIN message to server");
//
//            try {
//                Boolean response = Utils.read(Boolean.class, in);
//                if (response == null) {
//                    System.err.println("Error during login protocol: cannot read result as a Boolean");
//                    return;
//                }
//
//                if (response) {
//                    System.out.println("Identified!");
//                } else {
//                    System.out.println("Not identified!");
//                }
//
//            } catch (ClassNotFoundException e) {
//                System.err.println(e.getMessage());
//            }
//
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }
//    }
}
