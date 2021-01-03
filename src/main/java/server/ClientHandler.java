package server;

import mailer.InvalidMailAddressException;
import mailer.Mail;
import mailer.MailAddress;
import mailer.connections.ConnectionHandler;
import mailer.messages.*;
import server.exceptions.InvalidIDException;
import server.exceptions.NoSuchAddressException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

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
            // If we cannot read the message send an error to the client
            // to avoid leaving it hanging
            // TODO: always check send message return value
            sendMessage(new ErrorMessage("Unable to correctly read message"));
            logger.print("[%s] - error reading message");
            return;
        }

        // Process message
        // TODO: return boolean flag to signify failure
        processMessage(msg);

        closeConnection();
        logger.print("[%s] - closing connection", address);
    }

    private void processMessage(Message message) {
        switch (message.getType()) {
            case LOGIN: {
                LoginMessage loginMessage = castMessage(LoginMessage.class, message);
                // If it's null there is a mismatch between message type and class. This is a bug.
                // Fix it in LoginMessage class
                assert loginMessage != null;

                processLogin(loginMessage);
            }
            break;

            case FETCH_REQUEST: {
                MailFetchRequestMessage fetchRequestMessage = castMessage(MailFetchRequestMessage.class, message);
                // If it's null there is a mismatch between message type and class. This is a bug.
                // Fix it in MailFetchRequestMessage class
                assert fetchRequestMessage != null;

                processFetchRequestMessage(fetchRequestMessage);
            }
            break;

            case MAIL_PUSH: {
                MailPushMessage mailPushMessage = castMessage(MailPushMessage.class, message);
                // If it's null there is a mismatch between message type and class. This is a bug.
                // Fix it in MailPushMessage class
                assert mailPushMessage != null;

                processMailPushMessage(mailPushMessage);
            }
            break;

            case MAIL_DELETE: {
                MailDeleteMessage mailDeleteMessage = castMessage(MailDeleteMessage.class, message);
                // If it's null there is a mismatch between message type and class. This is a bug.
                // Fix it in MailDelete class
                assert mailDeleteMessage != null;

                processMailDelete(mailDeleteMessage);
            }
            break;

            case ERROR:
            case SUCCESS:
                // TODO: is it a programmer error?
                // If server receives a success or error message
                // without any context it doesn't do anything
                assert false;
                break;
        }
    }

    private void processMailDelete(MailDeleteMessage mailDeleteMessage) {
        logger.print("[%s] - received %s message", address, mailDeleteMessage.getType());

        try {
            boolean result = mailManager.deleteMail(mailDeleteMessage.getUser(), mailDeleteMessage.getMailID());
            if (result) {
                // Delete was successful
                sendMessage(new Success());
            } else {
                // Delete failed
                logger.print("[%s] - delete operation has failed", address);
                sendMessage(new ErrorMessage("Delete operation failed"));
            }
        } catch (NoSuchAddressException e) {
            logger.print("[%s] - %s", e.getMessage());
            sendMessage(new ErrorMessage(String.format("Invalid mail address '%s'", mailDeleteMessage.getUser())));
            e.printStackTrace();
        } catch (InvalidIDException e) {
            logger.print("[%s] - invalid mail id '%s'", address, e.getId());
            sendMessage(new ErrorMessage("Invalid mail ID"));
            e.printStackTrace();
        }

    }

    private void processMailPushMessage(MailPushMessage mailPushMessage) {
        logger.print("[%s] - received %s message", address, mailPushMessage.getType());

        try {
            mailManager.process(mailPushMessage.getMail());
        } catch (NoSuchAddressException e) {
            sendMessage(new ErrorMessage(e.getMessage()));
            return;
        }

        sendMessage(new Success());
    }

    private void processFetchRequestMessage(MailFetchRequestMessage fetchRequestMessage) {
        logger.print("[%s] - received %s message", address, fetchRequestMessage.getType());

        String addressString = fetchRequestMessage.getMailAddress();

        try {
            MailAddress mailAddress = new MailAddress(addressString);
            if (!mailManager.verify(mailAddress)) {
                logger.print("[%s] - user '%s' not registered", address, addressString);
                sendMessage(new ErrorMessage(
                        String.format("Mail address '%s' is not registered", addressString)
                ));
                return;
            }

            Mail[] mails = mailManager.loadMails(mailAddress, Arrays.asList(fetchRequestMessage.getReceived()));
            if (mails == null) {
                logger.print("[%s] - error loading mails from storage", address);
                sendMessage(new ErrorMessage("Error loading mails"));
                return;
            }

            sendMessage(new MailFetchResponseMessage(mails));

        } catch (InvalidMailAddressException e) {
            // Mail address is invalid, send error message back to the client
            logger.print("[%s] - %s", address, e.getMessage());
            sendMessage(new ErrorMessage(String.format("Invalid mail address '%s'", addressString))
            );

            e.printStackTrace();
        }
    }

    private void processLogin(LoginMessage loginMessage) {
        logger.print("[%s] - received %s message", address, loginMessage.getType());

        // Verify mail address and log result
        boolean result = mailManager.verify(loginMessage.getMailAddress());
        logger.print("[%s] - user is %s registered", address, result ? "" : "not");

        if (result) {
            sendMessage(new Success());
        } else {
            sendMessage(new ErrorMessage("Unknown mail"));
        }
    }
}
