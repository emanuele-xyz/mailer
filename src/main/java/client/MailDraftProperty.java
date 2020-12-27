package client;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;
import mailer.InvalidMailAddressException;
import mailer.Mail;
import mailer.MailAddress;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MailDraftProperty {

    private final MailAddress user;
    private final SimpleStringProperty subject;
    private final List<SimpleStringProperty> tos;
    private final SimpleStringProperty text;

    public MailDraftProperty(MailAddress user) {
        this.user = user;
        subject = new SimpleStringProperty();
        tos = new ArrayList<>();
        text = new SimpleStringProperty();
    }

    public void clear() {
        subject.set("");
        tos.clear();
        text.set("");
    }

    public Mail makeMail() throws InvalidMailAddressException {
        UUID id = UUID.randomUUID();
        List<MailAddress> recipients = getRecipients();

        return new Mail(
                id,
                user,
                recipients,
                new Date(),
                subject.getValue().trim(),
                text.getValue().trim()
        );
    }

    public SimpleStringProperty addRecipient() {
        SimpleStringProperty tmp = new SimpleStringProperty();
        tos.add(tmp);
        return tmp;
    }

    public void removeRecipient(SimpleStringProperty prop) {
        tos.remove(prop);
    }

    public SimpleStringProperty subjectProperty() {
        return subject;
    }

    public List<SimpleStringProperty> getTos() {
        return tos;
    }

    public SimpleStringProperty textProperty() {
        return text;
    }

    private List<MailAddress> getRecipients() throws InvalidMailAddressException {
        List<String> addresses = tos.stream()
                .map(StringPropertyBase::get)
                .distinct().filter(s -> !s.equals(user.toString()))
                .collect(Collectors.toList());

        List<MailAddress> result = new ArrayList<>();
        for (String address : addresses) {
            MailAddress tmp = new MailAddress(address);
        }
        return result;
    }
}
