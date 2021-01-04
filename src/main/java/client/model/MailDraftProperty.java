package client.model;

import client.exceptions.InvalidRecipientsException;
import client.exceptions.InvalidSubjectException;
import client.exceptions.InvalidTextException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mailer.InvalidMailAddressException;
import mailer.Mail;
import mailer.MailAddress;

import java.util.*;
import java.util.stream.Collectors;

public final class MailDraftProperty {

    private final MailAddress user;
    private final SimpleStringProperty subject;
    private final ObservableList<SimpleStringProperty> tos;
    private final SimpleStringProperty text;
    private final SimpleBooleanProperty clear;

    public MailDraftProperty(MailAddress user) {
        this.user = user;
        subject = new SimpleStringProperty();
        tos = FXCollections.observableArrayList();
        text = new SimpleStringProperty();
        clear = new SimpleBooleanProperty(false);
    }

    public void clear() {
        subject.set("");
        tos.clear();
        text.set("");
        clear.set(true);
        clear.set(false);
    }

    public Mail makeMail() throws InvalidMailAddressException, InvalidSubjectException, InvalidTextException, InvalidRecipientsException {
        UUID id = UUID.randomUUID();

        String subject = this.subject.getValue().trim();
        if (subject.isEmpty()) {
            throw new InvalidSubjectException("Subject is empty");
        }

        List<MailAddress> recipients = getRecipients();
        if (recipients.isEmpty()) {
            throw new InvalidRecipientsException("There are no valid recipients");
        }

        String text = this.text.getValue().trim();
        if (text.isEmpty()) {
            throw new InvalidTextException("Mail has no text");
        }

        return new Mail(id, user, recipients, new Date(), subject, text);
    }

    public void addRecipient() {
        SimpleStringProperty tmp = new SimpleStringProperty();
        tos.add(tmp);
    }

    public void addRecipient(String recipient) {
        SimpleStringProperty tmp = new SimpleStringProperty(recipient);
        tos.add(tmp);
    }

    public void removeRecipient(SimpleStringProperty prop) {
        tos.remove(prop);
    }

    public SimpleStringProperty subjectProperty() {
        return subject;
    }

    public ObservableList<SimpleStringProperty> getTos() {
        return tos;
    }

    public SimpleStringProperty textProperty() {
        return text;
    }

    public SimpleBooleanProperty clearProperty() {
        return clear;
    }

    private List<MailAddress> getRecipients() throws InvalidMailAddressException, InvalidRecipientsException {
        List<String> addresses = tos.stream()
                .map(StringPropertyBase::get)
                .filter(Objects::nonNull)
                // We should not depend upon the fact that the string is trimmed
                .map(String::trim)
                .distinct()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        List<MailAddress> result = new ArrayList<>();
        for (String address : addresses) {
            result.add(new MailAddress(address));
        }

        if (result.contains(user)) {
            throw new InvalidRecipientsException(String.format("Invalid recipient: '%s'", user));
        }

        return result;
    }
}
