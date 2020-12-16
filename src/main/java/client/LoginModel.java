package client;

import javafx.beans.property.SimpleStringProperty;
import mailer.MailAddress;

public final class LoginModel {

    private final SimpleStringProperty username;
    private final SimpleStringProperty errorMessage;

    public LoginModel() {
        username = new SimpleStringProperty();
        errorMessage = new SimpleStringProperty();
    }

    public void tryLogin() {
        if (validateUsername()) {
            // TODO: Load main app window
            errorMessage.set("");
        } else {
            errorMessage.set("Error! Cannot login");
        }
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public SimpleStringProperty errorMessageProperty() {
        return errorMessage;
    }

    private boolean validateUsername() {
        // Trim the input text since user can mistype some
        // spaces at the start and at the end of the input field
        return MailAddress.validate(username.get());
    }
}
