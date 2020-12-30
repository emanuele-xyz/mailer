package client;

import javafx.beans.property.SimpleStringProperty;
import mailer.Mail;
import mailer.MailAddress;

public final class MailProperty {

    private final SimpleStringProperty subject;
    private final SimpleStringProperty from;
    private MailAddress fromAddress;
    private final SimpleStringProperty to;
    private MailAddress[] toAddresses;
    private final SimpleStringProperty date;
    private final SimpleStringProperty text;

    public MailProperty() {
        subject = new SimpleStringProperty("");
        from = new SimpleStringProperty("");
        fromAddress = null;
        to = new SimpleStringProperty("");
        toAddresses = null;
        date = new SimpleStringProperty("");
        text = new SimpleStringProperty("");
    }

    public void select(Mail mail) {
        subject.set(mail.getSubject());
        from.set(mail.getFrom().toString());
        fromAddress = mail.getFrom();
        to.set(getTo(mail));
        toAddresses = mail.getTo();
        date.set(mail.getDate().toString());
        text.set(mail.getText());
    }

    public SimpleStringProperty subjectProperty() {
        return subject;
    }

    public SimpleStringProperty fromProperty() {
        return from;
    }

    public MailAddress getFromAddress() {
        return fromAddress;
    }

    public SimpleStringProperty toProperty() {
        return to;
    }

    public MailAddress[] getToAddresses() {
        return toAddresses;
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
