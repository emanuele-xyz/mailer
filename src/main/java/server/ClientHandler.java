package server;

import mailer.exceptions.InvalidMailAddressException;
import mailer.Mail;
import mailer.MailAddress;
import mailer.connections.ConnectionHandler;
import mailer.messages.*;
import server.exceptions.InvalidIDException;
import server.exceptions.NoSuchAddressException;
import server.logger.Logger;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * ClientHandler manages a connection to a client.
 * It is responsible for closing the socket and data streams
 */
public final class ClientHandler extends ConnectionHandler implements Runnable {

    private final String address;
    private final MailManager mailManager;
    private final Logger logger;

    public ClientHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out, String address, MailManager mailManager, Logger logger) {
        super(socket, in, out);
        this.address = address;
        this.mailManager = mailManager;
        this.logger = logger;
    }

    @Override
    public void run() {
        // Wait for client message
        Message msg = readMessage();
        if (msg == null) {
            // If we cannot read the message, try to send an error to the client
            // If sendMessage fails, we can't do anything. Hence, we don't check the
            // return value
            sendMessage(new ErrorMessage("Unable to correctly read message"));
            logger.print("[%s] - error reading message");
            return;
        }

        // Process message
        processMessage(msg);

        closeConnection();
        logger.print("[%s] - closing connection", address);
    }

    private void processMessage(Message message) {
        switch (message.getType()) {
            case LOGIN: {
                LoginMessage loginMessage = castMessage(LoginMessage.class, message);
                processLogin(loginMessage);
            }
            break;

            case FETCH_REQUEST: {
                MailFetchRequestMessage fetchRequestMessage = castMessage(MailFetchRequestMessage.class, message);
                processFetchRequestMessage(fetchRequestMessage);
            }
            break;

            case MAIL_PUSH: {
                MailPushMessage mailPushMessage = castMessage(MailPushMessage.class, message);
                processMailPushMessage(mailPushMessage);
            }
            break;

            case MAIL_DELETE: {
                MailDeleteMessage mailDeleteMessage = castMessage(MailDeleteMessage.class, message);
                processMailDelete(mailDeleteMessage);
            }
            break;

            case ERROR:
            case SUCCESS:
                // If server receives a success or error message
                // without any context it doesn't do anything
                // This is a programmer error, the client should never send such a message
                logger.print("[%s] - received '%s' message: do nothing", address, message.getType());
                assert false;
                break;
        }
    }

    /**
     * Logic for processing a mail delete message
     * @param mailDeleteMessage the message to be processed
     */
    private void processMailDelete(MailDeleteMessage mailDeleteMessage) {
        logger.print("[%s] - received %s message", address, mailDeleteMessage.getType());

        try {
            boolean result = mailManager.deleteMail(mailDeleteMessage.getUser(), mailDeleteMessage.getMailID());
            if (result) {
                // Delete was successful
                logger.print("[%s] - mail delete succeeded", address);
                sendMessage(new Success());
            } else {
                // Delete failed
                logger.print("[%s] - mail delete failed", address);
                sendMessage(new ErrorMessage("Delete operation failed"));
            }
        } catch (NoSuchAddressException e) {
            logger.print("[%s] - %s", e.getMessage());
            sendMessage(new ErrorMessage(String.format("Invalid mail address '%s'", mailDeleteMessage.getUser())));
        } catch (InvalidIDException e) {
            logger.print("[%s] - invalid mail id '%s'", address, e.getId());
            sendMessage(new ErrorMessage("Invalid mail ID"));
            e.printStackTrace();
        }
    }

    /**
     * Logic for processing a mail push message
     * @param mailPushMessage the message to be processed
     */
    private void processMailPushMessage(MailPushMessage mailPushMessage) {
        logger.print("[%s] - received %s message", address, mailPushMessage.getType());

        try {
            mailManager.process(mailPushMessage.getMail());
        } catch (NoSuchAddressException e) {
            logger.print("[%s] - %s", address, e.getMessage());
            sendMessage(new ErrorMessage(e.getMessage()));
            return;
        }

        logger.print("[%s] - mail push succeeded", address);
        sendMessage(new Success());
    }

    /***
     * Logic for processing a mail fetch request message
     * @param fetchRequestMessage the message to be processed
     */
    private void processFetchRequestMessage(MailFetchRequestMessage fetchRequestMessage) {
        logger.print("[%s] - received %s message", address, fetchRequestMessage.getType());

        String addressString = fetchRequestMessage.getMailAddress();
        try {
            MailAddress mailAddress = new MailAddress(addressString);
            if (!mailManager.verify(mailAddress)) {
                logger.print("[%s] - user '%s' not registered", address, addressString);
                sendMessage(new ErrorMessage(String.format("Mail address '%s' is not registered", addressString)));
                return;
            }

            Mail[] mails = mailManager.loadMails(mailAddress);
            // If there are no mails this should be an empty array, not a null array
            assert mails != null;

            logger.print("[%s] - mail fetch succeeded", address);
            sendMessage(new MailFetchResponseMessage(mails));

        } catch (InvalidMailAddressException e) {
            // Mail address is invalid, send error message back to the client
            logger.print("[%s] - %s", address, e.getMessage());
            sendMessage(new ErrorMessage(String.format("Invalid mail address '%s'", addressString)));
        } catch (NoSuchAddressException e) {
            // There is no account associated with mailAddress
            logger.print("[%s] - %s", address, e.getMessage());
            sendMessage(new ErrorMessage(e.getMessage()));
        }
    }

    /**
     * Logic for processing a login message
     * @param loginMessage the message to be processed
     */
    private void processLogin(LoginMessage loginMessage) {
        logger.print("[%s] - received %s message", address, loginMessage.getType());

        // Verify mail address and log result
        boolean result = mailManager.verify(loginMessage.getMailAddress());
        logger.print("[%s] - user is %s registered", address, result ? "" : "not");

        if (result) {
            logger.print("[%s] - login succeeded", address);
            sendMessage(new Success());
        } else {
            logger.print("[%s] - login failed", address);
            sendMessage(new ErrorMessage("Unknown mail"));
        }
    }
}
