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

/**
 * Model of main ui view
 */
public final class MainModel {

    private static final int TASK_RUNNER_THREADS = Math.max(Constants.CORES / 2, 1);

    private static final int MAIL_FETCH_THREADS = 1;
    private static final long MAIL_FETCH_PERIOD = 2;
    private static final TimeUnit MAIL_FETCH_TIME_UNIT = TimeUnit.SECONDS;

    private final MailAddress user;
    private final StringPropertyLogger logger;
    private final MainModelStateProperty currentState;
    /** true if a send task is currently running */
    private final SimpleBooleanProperty isSending;
    /** true if a delete task is currently running */
    private final SimpleBooleanProperty isDeleting;
    private final ObservableList<Mail> mails;
    private final MailProperty selectedMail;
    private final MailDraftProperty mailDraft;
    private final SimpleIntegerProperty newMailsReceived;

    private final ServerDispatcher serverDispatcher;
    private final ExecutorService tasksExecutor;
    private final ScheduledExecutorService mailFetchExecutor;

    public MainModel(MailAddress user) throws UnknownHostException {
        this.user = user;
        logger = new StringPropertyLogger();
        currentState = new MainModelStateProperty();
        isSending = new SimpleBooleanProperty(false);
        isDeleting = new SimpleBooleanProperty(false);
        mails = FXCollections.observableArrayList();
        selectedMail = new MailProperty();
        mailDraft = new MailDraftProperty(user);
        newMailsReceived = new SimpleIntegerProperty(0);

        serverDispatcher = new ServerDispatcher();
        tasksExecutor = Executors.newFixedThreadPool(TASK_RUNNER_THREADS);
        mailFetchExecutor = Executors.newScheduledThreadPool(MAIL_FETCH_THREADS);

        startFetchMailsService();
    }

    /**
     * Close the model
     */
    public void close() {
        serverDispatcher.shutdown();
        tasksExecutor.shutdown();
        mailFetchExecutor.shutdown();
    }

    /**
     * Issue a send mail task
     */
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

    /**
     * Set up model for reply
     */
    public void reply() {
        MailAddress from = selectedMail.getFromAddress();
        String subject = selectedMail.subjectProperty().get();

        reply(subject, selectedMail.toString(), from);
    }

    /**
     * Set up model for reply all
     */
    public void replyAll() {
        MailAddress from = selectedMail.getFromAddress();
        MailAddress[] to = selectedMail.getToAddresses();
        String subject = selectedMail.subjectProperty().get();

        reply(subject, selectedMail.toString(), from, to);
    }

    /**
     * Set up state for forward
     */
    public void forward() {
        String subject = selectedMail.subjectProperty().get();
        String text = selectedMail.textProperty().get();

        clearDraft();

        mailDraft.subjectProperty().set(subject);
        mailDraft.textProperty().set(text);

        currentState.setComposing();
    }

    /**
     * Issue a delete mail task
     */
    public void deleteMail() {
        Mail mail = selectedMail.getMail();
        if (mail == null) {
            logger.error("No mail is currently selected");
            return;
        }

        isDeleting.set(true);
        tasksExecutor.submit(new MailDeleteTask(
                serverDispatcher,
                logger,
                user,
                mail.getId(),
                () -> Platform.runLater(() -> onMailDeleted(mail)),
                () -> Platform.runLater(() -> isDeleting.set(false))
        ));
    }

    /**
     * Clear mail draft
     */
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

    public SimpleBooleanProperty isDeletingProperty() {
        return isDeleting;
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

    /**
     * Start service that fetches mails periodically
     */
    private void startFetchMailsService() {
        mailFetchExecutor.scheduleAtFixedRate(new MailsFetchTask(
                serverDispatcher,
                logger,
                user.toString(),
                receivedMails -> Platform.runLater(() -> {
                    // Remove mails that, for whatever reason, we have and the server hasn't
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

    /**
     * Set up main model for a reply.
     * This is used to implement reply and reply all
     * @param subject the subject of the mail we are replying to
     * @param text the text of the mail we are replying to
     * @param from the sender of the mail we are replying to
     * @param recipients the recipients of the mail we are replying to
     */
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

    /**
     * Callback used when mail is successfully deleted.
     * This should be called only in JavaFX thread since this method updates
     * properties bounded to the ui
     * @param mail the deleted mail
     */
    private void onMailDeleted(Mail mail) {
        mails.remove(mail);
        selectedMail.clear();
        currentState.setBlank();
    }
}
