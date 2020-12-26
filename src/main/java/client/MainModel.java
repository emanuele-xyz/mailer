package client;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import mailer.Mail;
import mailer.Utils;
import mailer.messages.ErrorMessage;
import mailer.messages.MailFetchRequestMessage;
import mailer.messages.Message;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Future;

public final class MainModel {

    private static final int MESSAGE_WAIT_TIME = 10 * 1000;

    private final String user;
    private final SimpleStringProperty errorMessage;
    private final SimpleListProperty<Mail> mails;

    private final ServerDispatcher serverDispatcher;

    public MainModel(String user) throws UnknownHostException {
        this.user = user;
        errorMessage = new SimpleStringProperty();
        mails = new SimpleListProperty<>();

        this.serverDispatcher = new ServerDispatcher();
    }

    public void close() {
        serverDispatcher.shutdown();
    }

    public String getUser() {
        return user;
    }

    public SimpleStringProperty errorMessageProperty() {
        return errorMessage;
    }
}
