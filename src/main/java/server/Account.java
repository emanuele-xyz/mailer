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
import java.util.stream.Collectors;

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

    public synchronized Mail[] loadMails(List<UUID> filter) {
        List<Mail> mails = new ArrayList<>();

        boolean result = loadMails(inboxDir, mails, filter);
        if (!result) {
            return null;
        }

        result = loadMails(outboxDir, mails, filter);
        if (!result) {
            return null;
        }

        return mails.toArray(new Mail[0]);
    }

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

    // Two threads could write to the same file concurrently: a disaster!
    private static void write(Mail mail, String box) {
        File file = new File(box, mail.getId().toString());

        if (file.exists()) {
            throw new Error(String.format("file '%s' already exists", file.getName()));
        }

        // TODO: find better handling later
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(MailJSONConverter.mailToJson(mail));
            out.flush();
        } catch (FileNotFoundException e) {
            // If there is no such file, it's a programming error
            // The programmer probably got the path wrong
            e.printStackTrace();
            assert false;
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

    private static boolean loadMails(String path, List<Mail> mails, List<UUID> filter) {
        File filePath = new File(path);
        String[] mailFilePaths = filePath.list((File current, String name) -> new File(current, name).isFile());
        if (mailFilePaths == null) {
            System.err.printf("Error loading emails in '%s' directory\n", path);
            return false;
        }

        List<String> filterPaths = filter.stream().map(UUID::toString).collect(Collectors.toList());
        for (String mailFilePath : mailFilePaths) {

            if (filterPaths.contains(mailFilePath)) {
                // If this mail is in the filter, don't load it
                // Get to the next mail
                continue;
            }

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
