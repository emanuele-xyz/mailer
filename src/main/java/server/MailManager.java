package server;

import mailer.Constants;
import mailer.InvalidMailAddressException;
import mailer.Mail;
import mailer.MailAddress;
import server.exceptions.MkdirException;
import server.exceptions.NoSuchAddressException;

import java.io.File;
import java.util.*;

public final class MailManager {

    private final Map<MailAddress, Account> accounts;

    public MailManager() throws MkdirException, InvalidMailAddressException {
        accounts = loadAccounts();
    }

    public boolean verify(String mailAddress) {
        try {
            MailAddress address = new MailAddress(mailAddress);
            return verify(address);
        } catch (InvalidMailAddressException e) {
            return false;
        }
    }

    public boolean verify(MailAddress mailAddress) {
        synchronized (accounts) {
            return accounts.containsKey(mailAddress);
        }
    }

    // To avoid killing parallelization we don't synchronize this method
    // on the entire email manager.
    // Each time we extract an account from 'accounts' we do that in mutual
    // exclusion.
    // Then, each 'send' and 'write' lock their account.
    public void process(Mail mail) throws NoSuchAddressException {
        MailAddress from = mail.getFrom();
        MailAddress[] to = mail.getTo();

        getAccount(from).send(mail);
        for (MailAddress recipient : to) {
            getAccount(recipient).receive(mail);
        }
    }

    public Mail[] loadMails(MailAddress address) {
        return accounts.get(address).loadMails();
    }

    private Account getAccount(MailAddress address) throws NoSuchAddressException {
        Account account;

        // synchronize access to shared resource
        synchronized (accounts) {
            account = accounts.get(address);
            if (account == null) {
                // This server doesn't know of such an email address
                throw new NoSuchAddressException(String.format("Unknown mail address '%s'", address));
            }
        }
        return account;
    }

    private static Map<MailAddress, Account> loadAccounts() throws MkdirException, InvalidMailAddressException {
        String[] dirs = listAccountsDirectories();
        Map<MailAddress, Account> tmp = new HashMap<>(dirs.length);

        for (String dir : dirs) {
            tmp.put(new MailAddress(dir), new Account(Constants.SERVER_DIRECTORY + File.separator + dir));
        }

        return tmp;
    }

    private static String[] listAccountsDirectories() throws MkdirException {
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
