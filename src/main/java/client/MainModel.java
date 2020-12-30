package client;

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
import mailer.InvalidMailAddressException;
import mailer.Mail;
import mailer.MailAddress;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainModel {

    private static final int MAIL_FETCH_THREADS = 1;
    private static final int MAIL_SEND_THREADS = 1;

    private final MainModelStateProperty currentState;
    private final MailAddress user;
    private final SimpleStringProperty errorMessage;
    private final SimpleBooleanProperty isSending;
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
        isSending = new SimpleBooleanProperty(false);
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
            Mail mail = mailDraft.makeMail();
            isSending.set(true);
            mailSenderExecutor.submit(new MailSendTask(
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
