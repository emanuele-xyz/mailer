package mailer;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public final class Mail implements Serializable {

    private static final Gson GSON = new Gson();

    public static Mail fromJson(String json) {
        return GSON.fromJson(json, Mail.class);
    }

    private final int id;
    private final MailAddress from;
    private final List<MailAddress> to;
    private final Date date;
    private final String subject;
    private final String text;

    public Mail(int id, MailAddress from, List<MailAddress> to, Date date, String subject, String text) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.date = date;
        this.subject = subject;
        this.text = text;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    @Override
    public String toString() {
        return "Mail{" +
                "id=" + id +
                ", from=" + from +
                ", to=" + to +
                ", date=" + date +
                ", subject='" + subject + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
