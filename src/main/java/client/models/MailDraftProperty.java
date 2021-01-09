package client.models;

import client.exceptions.InvalidRecipientsException;
import client.exceptions.InvalidSubjectException;
import client.exceptions.InvalidTextException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mailer.exceptions.IllegalMailException;
import mailer.exceptions.InvalidMailAddressException;
import mailer.Mail;
import mailer.MailAddress;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MailDraftProperty models a draft for a mail
 */
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

    /**
     * Clear the draft
     */
    public void clear() {
        subject.set("");
        tos.clear();
        text.set("");

        // Why this obscenity?
        // 'clear' is not used as a property, it's more like a trigger
        clear.set(true);
        clear.set(false);
    }

    /**
     * Create a new mail using the draft
     * @return the newly created mail
     * @throws InvalidMailAddressException thrown if there is at least one invalid mail address as a recipient
     * @throws InvalidSubjectException thrown if the draft's subject is blank
     * @throws InvalidTextException thrown if the draft's text is blank
     * @throws InvalidRecipientsException thrown if there are no recipients
     * @throws IllegalMailException thrown if sender mail address is also a recipient
     */
    public Mail makeMail() throws InvalidMailAddressException, InvalidSubjectException, InvalidTextException, InvalidRecipientsException, IllegalMailException {

        String subject = this.subject.getValue().trim();
        if (subject.isEmpty()) {
            throw new InvalidSubjectException("Subject is empty");
        }

        List<MailAddress> recipients = getRecipients();
        if (recipients.isEmpty()) {
            throw new InvalidRecipientsException("Recipients list is empty");
        }

        String text = this.text.getValue().trim();
        if (text.isEmpty()) {
            throw new InvalidTextException("Mail has no text");
        }

        return new Mail(user, recipients, subject, text);
    }

    public void addRecipient() {
        SimpleStringProperty tmp = new SimpleStringProperty();
        tos.add(tmp);
    }

    public void addRecipient(String recipient) {
        SimpleStringProperty tmp = new SimpleStringProperty(recipient.trim());
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

    /**
     * Get the list of recipients for this mail
     * @return the recipients list
     * @throws InvalidMailAddressException thrown if there is at least one invalid mail
     * address as a recipient
     * @throws InvalidRecipientsException thrown if there are duplicate recipients
     */
    private List<MailAddress> getRecipients() throws InvalidMailAddressException, InvalidRecipientsException {
        List<String> addresses = tos.stream()
                .map(StringPropertyBase::get)
                .filter(Objects::nonNull)
                // We depend upon the fact that recipient strings are already trimmed
                //.map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        addresses = getDistinct(addresses);

        List<MailAddress> recipients = new ArrayList<>();
        for (String address : addresses) {
            recipients.add(new MailAddress(address));
        }

        return recipients;
    }

    /**
     * Remove duplicates from a list of string mail addresses
     * @param addresses the list to remove duplicates from
     * @return the list without duplicates
     * @throws InvalidRecipientsException thrown if at least a duplicate is found
     */
    private static List<String> getDistinct(List<String> addresses) throws InvalidRecipientsException {
        Set<String> set = new HashSet<>();
        for (String address : addresses) {
            boolean alreadyPresent = !set.add(address);
            if (alreadyPresent) {
                throw new InvalidRecipientsException(String.format("Duplicate recipient '%s'", address));
            }
        }

        return new ArrayList<>(set);
    }
}
