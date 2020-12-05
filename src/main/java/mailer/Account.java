package mailer;

import java.util.ArrayList;

public final class Account {

    private final MailAddress address;
    private final ArrayList<Mail> sent;
    private final ArrayList<Mail> received;

    public Account(MailAddress address) {
        this.address = address;
        sent = new ArrayList<>();
        received = new ArrayList<>();
    }
}
