package mailer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class Mail implements Serializable {

    private final UUID id;
    private final MailAddress from;
    private final List<MailAddress> to;
    private final Date date;
    private final String subject;
    private final String text;

    public Mail(UUID id, MailAddress from, List<MailAddress> to, Date date, String subject, String text) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.date = date;
        this.subject = subject;
        this.text = text;
    }

    public UUID getId() {
        return id;
    }

    public MailAddress getFrom() {
        return from;
    }

    public MailAddress[] getTo() {
        // Why an array instead of a list?
        // I want my Mail objects to be immutable!
        MailAddress[] to = new MailAddress[this.to.size()];
        return this.to.toArray(to);
    }

    public Date getDate() {
        return date;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return subject;
    }
}
