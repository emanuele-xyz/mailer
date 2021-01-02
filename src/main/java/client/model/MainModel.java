package client.model;

import client.Logger;
import client.ServerDispatcher;
import client.exceptions.InvalidRecipientsException;
import client.exceptions.InvalidSubjectException;
import client.exceptions.InvalidTextException;
import client.tasks.MailSendTask;
import client.tasks.MailsFetchTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mailer.Constants;
import mailer.InvalidMailAddressException;
import mailer.Mail;
import mailer.MailAddress;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainModel {

    private static final int TASK_RUNNER_THREADS = Constants.CORES / 2;

    private final SimpleStringProperty errorMessage;
    private final Logger logger;

    private final MainModelStateProperty currentState;
    private final MailAddress user;
    private final SimpleBooleanProperty isSending;
    private final ObservableList<Mail> mails;
    private final MailProperty selectedMail;
    private final MailDraftProperty mailDraft;

    private final ServerDispatcher serverDispatcher;
    private final ExecutorService tasksExecutor;

    public MainModel(MailAddress user) throws UnknownHostException {
        errorMessage = new SimpleStringProperty();
        logger = new Logger(errorMessage);
        currentState = new MainModelStateProperty();
        this.user = user;
        isSending = new SimpleBooleanProperty(false);
        mails = FXCollections.observableArrayList();
        selectedMail = new MailProperty();
        mailDraft = new MailDraftProperty(user);

        serverDispatcher = new ServerDispatcher();
        tasksExecutor = Executors.newFixedThreadPool(TASK_RUNNER_THREADS);

        fetchMails();
    }

    public void close() {
        serverDispatcher.shutdown();
        tasksExecutor.shutdown();
    }

    public void fetchMails() {
        tasksExecutor.submit(new MailsFetchTask(
                serverDispatcher,
                logger,
                (mail) -> Platform.runLater(() -> mails.add(mail)),
                user.toString())
        );
    }

    public void sendMail() {
        try {
            Mail mail = mailDraft.makeMail();
            isSending.set(true);
            tasksExecutor.submit(new MailSendTask(
                    mail,
                    serverDispatcher,
                    logger,
                    () -> Platform.runLater(() -> mails.add(mail)),
                    () -> Platform.runLater(() -> isSending.set(false))
            ));
        } catch (InvalidMailAddressException | InvalidSubjectException | InvalidTextException | InvalidRecipientsException e) {
            logger.print(e.getMessage());
        }
    }

    public void reply() {
        MailAddress from = selectedMail.getFromAddress();
        String subject = selectedMail.subjectProperty().get();

        reply(subject, selectedMail.toString(), from);
    }

    public void replyAll() {
        MailAddress from = selectedMail.getFromAddress();
        MailAddress[] to = selectedMail.getToAddresses();
        String subject = selectedMail.subjectProperty().get();

        reply(subject, selectedMail.toString(), from, to);
    }

    public void forward() {
        String subject = selectedMail.subjectProperty().get();
        String text = selectedMail.textProperty().get();

        clearDraft();

        mailDraft.subjectProperty().set(subject);
        mailDraft.textProperty().set(text);

        currentState.setComposing();
    }

    public void deleteMail() {
        System.err.println("To be implemented");
    }

    public void clearDraft() {
        mailDraft.clear();
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

    public SimpleBooleanProperty isSendingProperty() {
        return isSending;
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

    private void reply(String subject, String text, MailAddress from, MailAddress... recipients) {
        if (from.equals(user)) {
            logger.print("Cannot reply to a mail that you have sent");
            return;
        }

        // Clear draft
        clearDraft();

        // Add to draft subject
        mailDraft.subjectProperty().set(subject);

        // Add to draft recipients
        mailDraft.addRecipient(from.toString());
        Arrays.stream(recipients)
                .filter(mailAddress -> !mailAddress.equals(user))
                .forEach(mailAddress -> mailDraft.addRecipient(mailAddress.toString()));

        // Add to draft text
        String tmp = String.format("\n\n\nReplying to\n%s\n", text);
        mailDraft.textProperty().set(tmp);

        // Change state to composing
        currentState.setComposing();
    }
}
