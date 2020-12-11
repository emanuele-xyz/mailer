package server;

import mailer.Constants;
import mailer.InvalidMailAddressException;
import mailer.Mail;
import mailer.MailAddress;

import java.io.File;
import java.util.*;

public final class MailManager {

    public static void main(String[] args) throws MkdirException, InvalidMailAddressException {
        Mail mail = new Mail(
                781263,
                new MailAddress("luca", "mailer.xyz"),
                Arrays.asList(
                        new MailAddress("marco", "mailer.xyz"),
                        new MailAddress("matteo", "mailer.xyz")),
                new Date(),
                "Important info!",
                "Hi, I have important things to discuss with you. Would you come to my place this evening?"
        );

        MailManager manager = new MailManager();
        manager.process(mail);
    }

    private final Map<MailAddress, Account> accounts;

    public MailManager() throws MkdirException, InvalidMailAddressException {
        accounts = loadAccounts();
    }

    // TODO: I have to make this method thread safe
    // TODO: We don't want multiple threads to interfere during the processing of a mail
    // TODO: We also don't want to kill parallelization blocking the entire mail manager
    // TODO: How should I handle concurrency?
    public void process(Mail mail) {
        MailAddress from = mail.getFrom();
        MailAddress[] to = mail.getTo();

        // TODO: what happens when there is no such account?
        accounts.get(from).send(mail);
        for (MailAddress recipient : to) {
            accounts.get(recipient).receive(mail);
        }
    }

    private Map<MailAddress, Account> loadAccounts() throws MkdirException, InvalidMailAddressException {
        String[] dirs = listAccountsDirectories();
        Map<MailAddress, Account> tmp = new HashMap<>(dirs.length);

        for (String dir : dirs) {
            tmp.put(new MailAddress(dir), new Account(Constants.SERVER_DIRECTORY + File.separator + dir));
        }

        return tmp;
    }

    private String[] listAccountsDirectories() throws MkdirException {
        File dir = new File(Constants.SERVER_DIRECTORY);
        if (!dir.exists()) {
            boolean result = dir.mkdir();
            if (!result) {
                throw new MkdirException("Unable to create directory '" + dir.toString() + "'");
            }
        }

        return dir.list((File current, String name) -> new File(current, name).isDirectory());
    }
}
