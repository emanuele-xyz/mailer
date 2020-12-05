module mailer {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    opens client;
    opens server;
}