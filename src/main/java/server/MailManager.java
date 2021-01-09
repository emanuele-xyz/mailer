package server;

import mailer.Constants;
import mailer.Mail;
import mailer.MailAddress;
import mailer.exceptions.InvalidMailAddressException;
import server.exceptions.InvalidIDException;
import server.exceptions.MkdirException;
import server.exceptions.NoSuchAddressException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MailManager handles and executes operations on mail server accounts
 */
public final class MailManager {

    private final Map<MailAddress, Account> accounts;

    public MailManager() throws MkdirException, InvalidMailAddressException {
        accounts = loadAccounts();
    }

    /**
     * Check whether a mail address is associated with some account
     *
     * @param mailAddress the string representing a mail address
     * @return true if the mail address is valid, false otherwise
     */
    public boolean verify(String mailAddress) {
        try {
            MailAddress address = new MailAddress(mailAddress);
            return verify(address);
        } catch (InvalidMailAddressException e) {
            return false;
        }
    }

    /**
     * Check whether a mail address is associated with some account
     *
     * @param mailAddress the string representing a mail address
     * @return true if the mail address is valid, false otherwise
     */
    public boolean verify(MailAddress mailAddress) {
        synchronized (accounts) {
            return accounts.containsKey(mailAddress);
        }
    }

    /**
     * Process a mail. This means inserting the mail into the sender
     * outbox, and inserting the mail into each recipient inbox
     *
     * @param mail the mail to be processed
     * @throws NoSuchAddressException thrown if at least one of the involved accounts is invalid
     */
    public void process(Mail mail) throws NoSuchAddressException {
        // To avoid killing parallelization we don't synchronize this method
        // on the entire email manager
        // Each time we extract an account from 'accounts' we do that in mutual exclusion
        // Then, each 'send' and 'write' locks their respective account

        MailAddress from = mail.getFrom();
        MailAddress[] to = mail.getTo();

        // First get all required accounts for this email to
        // be processed correctly.
        // If there there is an error getting an account
        // we don't write any mail to disk.
        Account fromAccount = getAccount(from);
        Account[] toAccounts = new Account[to.length];
        for (int i = 0; i < toAccounts.length; i++) {
            toAccounts[i] = getAccount(to[i]);
        }

        fromAccount.send(mail);
        for (Account recipient : toAccounts) {
            recipient.receive(mail);
        }
    }

    /**
     * Load all mails associated with an account
     *
     * @param address the account's address
     * @return the loaded mails if the load
     * @throws NoSuchAddressException if there is no account associated with <code>address</code>
     */
    public Mail[] loadMails(MailAddress address) throws NoSuchAddressException {
        return getAccount(address).loadMails();
    }

    /**
     * Delete a mail associated with an account
     *
     * @param address the account address
     * @param mailID  the id of the mail to be deleted
     * @return true if the delete succeeded, false otherwise
     * @throws NoSuchAddressException thrown if there is no account associated <code>address</code>
     * @throws InvalidIDException     thrown if there is no mail with the same <code>id</code>
     */
    public boolean deleteMail(MailAddress address, UUID mailID) throws NoSuchAddressException, InvalidIDException {
        Account account = getAccount(address);
        return account.deleteMail(mailID);
    }

    /**
     * Get the account associated with a mail address in a thread safe manner
     *
     * @param address a mail address
     * @return the associated account
     * @throws NoSuchAddressException thrown if there is no account for <code>address</code>
     */
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

    /**
     * Helper function for mail manager initialization.
     * Load all accounts stored in mail server file system
     *
     * @return a table that associates each known mail address to its mail account
     * @throws MkdirException              thrown if write to a directory went wrong
     * @throws InvalidMailAddressException thrown if an invalid mail address was retrieved
     */
    private static Map<MailAddress, Account> loadAccounts() throws MkdirException, InvalidMailAddressException {
        String[] dirs = listAccountsDirectories();
        Map<MailAddress, Account> tmp = new HashMap<>(dirs.length);

        for (String dir : dirs) {
            tmp.put(new MailAddress(dir), new Account(Constants.SERVER_DIRECTORY + File.separator + dir));
        }

        return tmp;
    }

    /**
     * Helper function for mail manager initialization.
     * List all account directories in mail server file system
     *
     * @return list of account directories
     * @throws MkdirException thrown if write to a directory went wrong
     */
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
