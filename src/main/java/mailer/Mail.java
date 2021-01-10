package mailer;

import mailer.exceptions.IllegalMailException;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Mail represents a mail
 */
public final class Mail implements Serializable {

    private final UUID id;
    private final MailAddress from;
    private final List<MailAddress> to;
    /**
     * When the mail was sent
     */
    private final Date date;
    private final String subject;
    private final String text;

    /**
     * Initialize a newly allocated Mail
     *
     * @param from    the sender
     * @param to      the recipients
     * @param subject the subject
     * @param text    the text
     * @throws IllegalMailException thrown if sender is also a recipient
     */
    public Mail(MailAddress from, List<MailAddress> to, String subject, String text) throws IllegalMailException {

        if (to.contains(from)) {
            throw new IllegalMailException("Sender mail address is also a recipient");
        }

        this.id = UUID.randomUUID();
        this.from = from;
        this.to = to;
        this.date = new Date();
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
        // We want mails to be immutable
        MailAddress[] to = new MailAddress[this.to.size()];
        return this.to.toArray(to);
    }

    public Date getDate() {
        // date is not immutable, we have to clone it to keep our
        // mail immutable
        return (Date) date.clone();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mail mail = (Mail) o;
        return id.equals(mail.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
