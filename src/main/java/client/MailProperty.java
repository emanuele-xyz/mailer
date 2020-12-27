package client;

import javafx.beans.property.SimpleStringProperty;
import mailer.Mail;
import mailer.MailAddress;

public final class MailProperty {

    private final SimpleStringProperty subject;
    private final SimpleStringProperty from;
    private final SimpleStringProperty to;
    private final SimpleStringProperty date;
    private final SimpleStringProperty text;

    public MailProperty() {
        subject = new SimpleStringProperty("");
        from = new SimpleStringProperty("");
        to = new SimpleStringProperty("");
        date = new SimpleStringProperty("");
        text = new SimpleStringProperty("");
    }

    public void select(Mail mail) {
        subject.set(mail.getSubject());
        from.set(mail.getFrom().toString());

        {
            StringBuilder sb = new StringBuilder();
            MailAddress[] tos = mail.getTo();
            for (int i = 0; i < tos.length; i++) {
                sb.append(tos[i].toString());
                if (i != tos.length - 1) {
                    sb.append(", ");
                }
            }
            to.set(sb.toString());
        }

        date.set(mail.getDate().toString());
        text.set(mail.getText());
    }

    public SimpleStringProperty subjectProperty() {
        return subject;
    }

    public SimpleStringProperty fromProperty() {
        return from;
    }

    public SimpleStringProperty toProperty() {
        return to;
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public SimpleStringProperty textProperty() {
        return text;
    }
}
