package server;

import mailer.Mail;
import mailer.MailJSONConverter;
import server.exceptions.InvalidIDException;
import server.exceptions.MkdirException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    /**
     * Save a mail in the account outbox
     *
     * @param mail the mail to save
     */
    public synchronized void send(Mail mail) {
        // Each mail has an unique id.
        // (Note that the same mail is stored in different places in different accounts!)
        // Hence processing the same mail (since mails are unique) more than once is an error.
        // But can we trust the client? Never trust the client! If we care about security
        // we should handle malicious clients that create mails with ids of previously processed mails.
        write(mail, outboxDir);
    }

    /**
     * Save a mail in the account inbox
     *
     * @param mail the mail to save
     */
    public synchronized void receive(Mail mail) {
        // Read 'send' method comment
        write(mail, inboxDir);
    }

    /**
     * Load all mails associated with this account
     *
     * @return a list of mails
     */
    public synchronized Mail[] loadMails() {
        List<Mail> mails = new ArrayList<>();

        boolean result = loadMails(inboxDir, mails);
        if (!result) {
            return null;
        }

        result = loadMails(outboxDir, mails);
        if (!result) {
            return null;
        }

        return mails.toArray(new Mail[0]);
    }

    /**
     * Delete a mail from this account
     *
     * @param mailID the mail's id
     * @return true if the delete was successful, false otherwise
     * @throws InvalidIDException thrown if there is no such mail in this account
     */
    public synchronized boolean deleteMail(UUID mailID) throws InvalidIDException {
        File mail = new File(inboxDir, mailID.toString());
        if (mail.exists()) {
            return mail.delete();
        }

        mail = new File(outboxDir, mailID.toString());
        if (mail.exists()) {
            return mail.delete();
        }

        throw new InvalidIDException(mailID);
    }

    @Override
    public String toString() {
        return accountDirectory;
    }

    /**
     * Write a mail to a box. Note that this is not thread safe since, in the
     * erroneous case of two mails with the same ids, two threads could write
     * the same file concurrently!
     *
     * @param mail the mail to be saved
     * @param box  the box directory
     */
    private static void write(Mail mail, String box) {
        File file = new File(box, mail.getId().toString());

        if (file.exists()) {
            // This should never happen, and if it does it means that our IDs are not unique!
            assert false;
            throw new Error(String.format("Mail '%s' already exists", file.getName()));
        }

        try (PrintWriter out = new PrintWriter(file)) {
            out.println(MailJSONConverter.mailToJson(mail));
            out.flush();
        } catch (FileNotFoundException e) {
            // If there is no such file, it's a programming error
            // The programmer probably got the inbox or outbox path wrong
            e.printStackTrace();
            assert false;
        }
    }

    /**
     * Create directory if <code>dir</code> is not present
     *
     * @param dir the directory
     * @throws MkdirException thrown if directory creation went wrong
     */
    private static void makeDirIfNotPresent(String dir) throws MkdirException {
        File inbox = new File(dir);
        if (inbox.exists()) {
            return;
        }

        boolean res = inbox.mkdir();
        if (!res) {
            throw new MkdirException(String.format("Unable to create '%s' directory", dir));
        }
    }

    /**
     * Load all mails associated with this account
     *
     * @param path  box path
     * @param mails where to store the loaded mails
     * @return true if the load was successful, false otherwise
     */
    private static boolean loadMails(String path, List<Mail> mails) {
        File filePath = new File(path);
        String[] mailFilePaths = filePath.list((File current, String name) -> new File(current, name).isFile());
        if (mailFilePaths == null) {
            // This is a programmer error
            assert false;
            throw new Error(String.format("Path '%s' is wrong", path));
        }

        for (String mailFilePath : mailFilePaths) {
            Path fileName = Path.of(path, mailFilePath);
            try {
                String json = Files.readString(fileName);
                Mail mail = MailJSONConverter.mailFromJson(json);
                mails.add(mail);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }
}
