package client.models;

import client.logger.StringPropertyLogger;
import client.ServerDispatcher;
import client.exceptions.InvalidRecipientsException;
import client.exceptions.InvalidSubjectException;
import client.exceptions.InvalidTextException;
import client.tasks.MailDeleteTask;
import client.tasks.MailSendTask;
import client.tasks.MailsFetchTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mailer.Constants;
import mailer.exceptions.IllegalMailException;
import mailer.exceptions.InvalidMailAddressException;
import mailer.Mail;
import mailer.MailAddress;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class MainModel {

    private static final int TASK_RUNNER_THREADS = Math.max(Constants.CORES / 2, 1);

    private static final int MAIL_FETCH_THREADS = 1;
    private static final long MAIL_FETCH_PERIOD = 2;
    private static final TimeUnit MAIL_FETCH_TIME_UNIT = TimeUnit.SECONDS;

    private final StringPropertyLogger logger;
    private final MainModelStateProperty currentState;
    private final MailAddress user;
    private final SimpleBooleanProperty isSending;
    private final ObservableList<Mail> mails;
    private final MailProperty selectedMail;
    private final MailDraftProperty mailDraft;
    private final SimpleIntegerProperty newMailsReceived;

    private final ServerDispatcher serverDispatcher;
    private final ExecutorService tasksExecutor;
    private final ScheduledExecutorService mailFetchExecutor;

    public MainModel(MailAddress user) throws UnknownHostException {
        logger = new StringPropertyLogger(new SimpleStringProperty(), new SimpleStringProperty());
        currentState = new MainModelStateProperty();
        this.user = user;
        isSending = new SimpleBooleanProperty(false);
        mails = FXCollections.observableArrayList();
        selectedMail = new MailProperty();
        mailDraft = new MailDraftProperty(user);
        newMailsReceived = new SimpleIntegerProperty(0);

        serverDispatcher = new ServerDispatcher();
        tasksExecutor = Executors.newFixedThreadPool(TASK_RUNNER_THREADS);
        mailFetchExecutor = Executors.newScheduledThreadPool(MAIL_FETCH_THREADS);

        startFetchMailsService();
    }

    public void close() {
        serverDispatcher.shutdown();
        tasksExecutor.shutdown();
        mailFetchExecutor.shutdown();
    }

    public void sendMail() {
        try {
            Mail mail = mailDraft.makeMail();
            isSending.set(true);
            tasksExecutor.submit(new MailSendTask(
                    serverDispatcher,
                    logger,
                    mail,
                    () -> Platform.runLater(() -> mails.add(0, mail)),
                    () -> Platform.runLater(() -> isSending.set(false))
            ));
        } catch (InvalidMailAddressException | InvalidSubjectException | InvalidTextException | InvalidRecipientsException | IllegalMailException e) {
            logger.error(e.getMessage());
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
        Mail mail = selectedMail.getMail();
        if (mail == null) {
            logger.error("No mail is currently selected");
            return;
        }

        tasksExecutor.submit(new MailDeleteTask(
                serverDispatcher,
                logger,
                user,
                mail.getId(),
                () -> Platform.runLater(() -> onMailDeleted(mail))
        ));
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

    public SimpleStringProperty successMessageProperty() {
        return logger.successMessageProperty();
    }

    public SimpleStringProperty errorMessageProperty() {
        return logger.errorMessageProperty();
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

    public SimpleIntegerProperty newMailsReceivedProperty() {
        return newMailsReceived;
    }

    private void startFetchMailsService() {
        mailFetchExecutor.scheduleAtFixedRate(new MailsFetchTask(
                serverDispatcher,
                logger,
                user.toString(),
                receivedMails -> Platform.runLater(() -> {
                    // Remove mails that for whatever reason we have and the server hasn't
                    // This is not impossible, we could issue and complete a mail delete request while we fetch mails
                    // This could lead to inconsistencies between client and server
                    List<Mail> toBeRemoved = mails.stream()
                            .filter(mail -> !receivedMails.contains(mail))
                            .collect(Collectors.toList());
                    toBeRemoved.forEach(mails::remove);

                    // Add new mails and notify for popup dialog
                    List<Mail> newMails = receivedMails.stream()
                            .filter(receivedMail -> !mails.contains(receivedMail))
                            .collect(Collectors.toList());

                    newMailsReceived.set(newMails.size());
                    newMailsReceived.set(0);

                    newMails.forEach(receivedMail -> mails.add(0, receivedMail));
                })
        ), 0, MAIL_FETCH_PERIOD, MAIL_FETCH_TIME_UNIT);
    }

    private void reply(String subject, String text, MailAddress from, MailAddress... recipients) {
        if (from.equals(user)) {
            logger.error("Cannot reply to a mail that you have sent");
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

    // This should be called only in JavaFX thread since this method
    // updates the ui
    private void onMailDeleted(Mail mail) {
        mails.remove(mail);
        selectedMail.clear();
        currentState.setBlank();
    }
}
