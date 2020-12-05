package mailer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public final class Mail implements Serializable {

    private final int id;
    private final MailAddress sender;
    private final ArrayList<Mail> receivers;
    private final Date date;
    private final String subject;
    private final String text;

    public Mail(int id, MailAddress sender, ArrayList<Mail> receivers, Date date, String subject, String text) {
        this.id = id;
        this.sender = sender;
        this.receivers = receivers;
        this.date = date;
        this.subject = subject;
        this.text = text;
    }
}
