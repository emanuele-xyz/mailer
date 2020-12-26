module mailer {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires com.google.gson;
    requires commons.validator;

    opens client;
    opens client.login;
    opens client.controllers;
    opens server;
    opens mailer;
}