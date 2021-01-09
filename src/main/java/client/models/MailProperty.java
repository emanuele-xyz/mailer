package client.models;

import javafx.beans.property.SimpleStringProperty;
import mailer.Mail;
import mailer.MailAddress;

/**
 * MailProperty is a custom property that represents a mail as a collection of properties
 */
public final class MailProperty {

    private Mail mail;
    private final SimpleStringProperty subject;
    private final SimpleStringProperty from;
    private final SimpleStringProperty to;
    private final SimpleStringProperty date;
    private final SimpleStringProperty text;

    public MailProperty() {
        mail = null;
        subject = new SimpleStringProperty("");
        from = new SimpleStringProperty("");
        to = new SimpleStringProperty("");
        date = new SimpleStringProperty("");
        text = new SimpleStringProperty("");
    }

    /**
     * Clear the mail property resetting all fields to default values
     */
    public void clear() {
        mail = null;
        subject.set("");
        from.set("");
        to.set("");
        date.set("");
        text.set("");
    }

    /**
     * Initialize the mail property with a mail
     *
     * @param mail the mail we are initializing from
     */
    public void select(Mail mail) {
        this.mail = mail;
        subject.set(mail.getSubject());
        from.set(mail.getFrom().toString());
        to.set(getTo(mail));
        date.set(mail.getDate().toString());
        text.set(mail.getText());
    }

    public Mail getMail() {
        return mail;
    }

    public SimpleStringProperty subjectProperty() {
        return subject;
    }

    public SimpleStringProperty fromProperty() {
        return from;
    }

    public MailAddress getFromAddress() {
        return mail.getFrom();
    }

    public SimpleStringProperty toProperty() {
        return to;
    }

    public MailAddress[] getToAddresses() {
        return mail.getTo();
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public SimpleStringProperty textProperty() {
        return text;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String border = "-".repeat(30);
        sb.append(border).append('\n');
        sb.append("Subject: ").append(subject.get()).append('\n');
        sb.append("From: ").append(from.get()).append('\n');
        sb.append("To: ").append(to.get()).append('\n');
        sb.append("Date: ").append(date.get()).append('\n');
        sb.append(border).append('\n');
        sb.append('\n');
        sb.append(text.get());
        sb.append('\n').append(border);
        return sb.toString();
    }

    /**
     * Get a string representation of a mail's to field
     */
    private String getTo(Mail mail) {
        StringBuilder sb = new StringBuilder();
        MailAddress[] tos = mail.getTo();
        for (int i = 0; i < tos.length; i++) {
            sb.append(tos[i].toString());
            if (i != tos.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
