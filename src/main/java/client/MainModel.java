package client;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mailer.Mail;
import mailer.MailAddress;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class MainModel {

    private static final int MAIL_FETCH_THREADS = 1;

    private final MainModelStateProperty currentState;
    private final MailAddress user;
    private final SimpleStringProperty errorMessage;
    private final ObservableList<Mail> mails;
    private final MailProperty selectedMail;
    private final MailDraftProperty mailDraft;

    private final Logger logger;
    private final ServerDispatcher serverDispatcher;
    private final ExecutorService mailFetcherExecutor;

    public MainModel(MailAddress user) throws UnknownHostException {
        currentState = new MainModelStateProperty();
        this.user = user;
        errorMessage = new SimpleStringProperty();
        mails = FXCollections.observableArrayList();
        selectedMail = new MailProperty();
        mailDraft = new MailDraftProperty(user);

        logger = new Logger(errorMessage);
        serverDispatcher = new ServerDispatcher();
        mailFetcherExecutor = Executors.newFixedThreadPool(MAIL_FETCH_THREADS);

        getMailsFromServer();
    }

    public void close() {
        serverDispatcher.shutdown();
        mailFetcherExecutor.shutdown();
    }

    public void send(Mail mail, Consumer<MailDraftProperty> onSuccess) {

    }

    public void setErrorMessage(String msg) {
        logger.print(msg);
    }

    public MainModelStateProperty getCurrentState() {
        return currentState;
    }

    public String getUser() {
        return user.toString();
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

    public MailDraftProperty getMailDraft() {
        return mailDraft;
    }

    private void getMailsFromServer() {
        mailFetcherExecutor.submit(new MailsFetchTask(
                serverDispatcher,
                logger,
                (mail) -> Platform.runLater(() -> mails.add(mail)),
                user.toString())
        );
    }
}
