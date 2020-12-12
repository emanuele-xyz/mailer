package server;

import mailer.Mail;
import server.exceptions.MkdirException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public final class Account {

    private static final String INBOX = "inbox";
    private static final String OUTBOX = "outbox";

    private final String inboxDir;
    private final String outboxDir;
    private final String accountDirectory;

    public Account(String accountDirectory) throws MkdirException {
        this.accountDirectory = accountDirectory;
        inboxDir = accountDirectory + File.separator + INBOX;
        outboxDir = accountDirectory + File.separator + OUTBOX;

        makeDirIfNotPresent(inboxDir);
        makeDirIfNotPresent(outboxDir);
    }

    // What if more than one thread tries to write the same email at the same time?
    // Each mail should have a unique ID.
    // Hence processing the same mail more than once sounds like an error.
    // But can we trust the client? Never trust the client!
    public synchronized void send(Mail mail) {
        write(mail, outboxDir);
    }

    // Read 'send' method comment
    public synchronized void receive(Mail mail) {
        write(mail, inboxDir);
    }

    @Override
    public String toString() {
        return accountDirectory;
    }

    // Two threads could write to the same file concurrently: a disaster!
    private static void write(Mail mail, String box) {
        File file = new File(box, mail.getId().toString());

        if (file.exists()) {
            throw new Error(String.format("file '%s' already exists", file.getName()));
        }

        // TODO: find better handling later
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(mail.toJson());
            out.flush();
        } catch (FileNotFoundException e) {
            // If there is no such file, it's a programming error
            // The programmer probably got the path wrong
            e.printStackTrace();
        }
    }

    private static void makeDirIfNotPresent(String dir) throws MkdirException {
        File inbox = new File(dir);
        if (!inbox.exists()) {
            boolean res = inbox.mkdir();
            if (!res) {
                throw new MkdirException(String.format("Unable to create '%s' directory", dir));
            }
        }
    }
}
