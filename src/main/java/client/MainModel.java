package client;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mailer.Mail;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainModel {

    private static final int MAIL_FETCH_THREADS = 1;

    private final String user;
    private final SimpleStringProperty errorMessage;
    private final ObservableList<Mail> mails;
    private final MailProperty selectedMail;

    private final Logger logger;
    private final ServerDispatcher serverDispatcher;
    private final ExecutorService mailFetcherExecutor;

    public MainModel(String user) throws UnknownHostException {
        this.user = user;
        errorMessage = new SimpleStringProperty();
        mails = FXCollections.observableArrayList();
        selectedMail = new MailProperty();

        logger = new Logger(errorMessage);
        this.serverDispatcher = new ServerDispatcher();
        mailFetcherExecutor = Executors.newFixedThreadPool(MAIL_FETCH_THREADS);

        getMailsFromServer();
    }

    public void close() {
        serverDispatcher.shutdown();
        mailFetcherExecutor.shutdown();
    }

    public String getUser() {
        return user;
    }

    public SimpleStringProperty errorMessageProperty() {
        return errorMessage;
    }

    public ObservableList<Mail> getMails() {
        return mails;
    }

    public MailProperty getSelectedMail() {
        return selectedMail;
    }

    private void getMailsFromServer() {
        mailFetcherExecutor.submit(new MailsFetchTask(
                serverDispatcher,
                logger,
                (mail) -> Platform.runLater(() -> mails.add(mail)),
                user)
        );
    }
}
