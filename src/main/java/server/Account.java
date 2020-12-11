package server;

import mailer.Mail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public final class Account {

    private static final String INBOX = "inbox";
    private static final String OUTBOX = "outbox";

    private final String inboxDir;
    private final String outboxDir;

    public Account(String accountDirectory) {
        inboxDir = accountDirectory + File.separator + INBOX;
        outboxDir = accountDirectory + File.separator + OUTBOX;

        boolean res = false;
        {
            File inbox = new File(inboxDir);
            if (!inbox.exists()) {
                // TODO: fix! This returns false
                res = inbox.mkdirs();
            }
        }

        {
            File outbox = new File(outboxDir);
            if (!outbox.exists()) {
                res = outbox.mkdirs();
            }
        }
    }

    public void send(Mail mail) {
        write(mail, outboxDir);
    }

    public void receive(Mail mail) {
        write(mail, inboxDir);
    }

    private void write(Mail mail, String box) {
        File file = new File(box, Integer.toString(mail.getId()));

        if (!file.exists())

        // TODO: find better handling later
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(mail.toJson());
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
