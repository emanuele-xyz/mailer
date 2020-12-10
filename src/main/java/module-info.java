module mailer {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires com.google.gson;

    opens client;
    opens server;
    opens mailer;
}