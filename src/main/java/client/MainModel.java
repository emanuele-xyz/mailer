package client;

import client.exceptions.InvalidRecipientsException;
import client.exceptions.InvalidSubjectException;
import client.exceptions.InvalidTextException;
import client.tasks.MailSendTask;
import client.tasks.MailsFetchTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mailer.InvalidMailAddressException;
import mailer.Mail;
import mailer.MailAddress;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class MainModel {

    private static final int MAIL_FETCH_THREADS = 1;
    private static final int MAIL_SEND_THREADS = 1;

    private final MainModelStateProperty currentState;
    private final MailAddress user;
    private final SimpleStringProperty errorMessage;
    private final ObservableList<Mail> mails;
    private final MailProperty selectedMail;
    private final MailDraftProperty mailDraft;

    private final Logger logger;
    private final ServerDispatcher serverDispatcher;
    private final ExecutorService mailFetcherExecutor;
    private final ExecutorService mailSenderExecutor;

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
        mailSenderExecutor = Executors.newFixedThreadPool(MAIL_SEND_THREADS);

        getMailsFromServer();
    }

    public void close() {
        serverDispatcher.shutdown();
        mailFetcherExecutor.shutdown();
        mailSenderExecutor.shutdown();
    }

    public void send() {
        try {
            Mail mail = mailDraft.makeMail();
            mailSenderExecutor.submit(new MailSendTask(mail, serverDispatcher, logger));
        } catch (InvalidMailAddressException | InvalidSubjectException | InvalidTextException | InvalidRecipientsException e) {
            logger.print(e.getMessage());
        }
    }

    // TODO: implement and add button to ui
    public void clearDraft() {

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
