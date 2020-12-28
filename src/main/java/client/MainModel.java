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

        fetchMails();
    }

    public void close() {
        serverDispatcher.shutdown();
        mailFetcherExecutor.shutdown();
        mailSenderExecutor.shutdown();
    }

    public void fetchMails() {
        mailFetcherExecutor.submit(new MailsFetchTask(
                serverDispatcher,
                logger,
                (mail) -> Platform.runLater(() -> mails.add(mail)),
                user.toString())
        );
    }

    public void sendMail() {
        try {
            // TODO: we have to disable send button and also state transitioning
            // TODO: enable send button and state transitioning when sending procedure has finished
            // TODO: mind that mail sending can fail and we have enable things even in this scenario
            // TODO: to do so add a onFinish callback to MailSendTask
            Mail mail = mailDraft.makeMail();
            mailSenderExecutor.submit(new MailSendTask(
                    mail,
                    serverDispatcher,
                    logger,
                    () -> Platform.runLater(() -> mails.add(mail))
            ));
        } catch (InvalidMailAddressException | InvalidSubjectException | InvalidTextException | InvalidRecipientsException e) {
            logger.print(e.getMessage());
        }
    }

    // TODO: implement
    public void reply() {
        System.err.println("To be implemented");

        // Get currently selected mail from field
        // Clear draft
        // Add to draft recipient
        // Change state to composing
    }

    // TODO: implement
    public void replyAll() {
        System.err.println("To be implemented");

        // Get currently selected mail from and to fields
        // Remove from to fields this account
        // Clear draft
        // Add to draft recipients
        // Change state to composing
    }

    // TODO: implement
    public void clearDraft() {
        System.err.println("To be implemented");
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
}
