package client.tasks;

import client.Logger;
import client.ServerDispatcher;
import mailer.Mail;
import mailer.Utils;
import mailer.messages.*;

import java.util.concurrent.Future;
import java.util.function.Consumer;

public class MailsFetchTask implements Runnable {

    private static final int MESSAGE_WAIT_TIME = 10 * 1000;

    private final ServerDispatcher serverDispatcher;
    private final Logger logger;
    private final Consumer<Mail> onMailReceived;
    private final String address;

    public MailsFetchTask(ServerDispatcher serverDispatcher, Logger logger, Consumer<Mail> onMailReceived, String address) {
        this.serverDispatcher = serverDispatcher;
        this.logger = logger;
        this.onMailReceived = onMailReceived;
        this.address = address;
    }

    @Override
    public void run() {
        Future<Message> response = serverDispatcher.sendToServer(new MailFetchRequestMessage(address), MESSAGE_WAIT_TIME);
        Message message = Utils.getResult(response);
        if (message == null) {
            logger.print("Error fetching mails from server! Try again");
            return;
        }

        switch (message.getType()) {
            case FETCH_RESPONSE: {
                MailFetchResponseMessage tmp = Utils.tryCast(MailFetchResponseMessage.class, message);
                assert tmp != null;
                for (Mail mail : tmp.getMails()) {
                    onMailReceived.accept(mail);
                }
            }
            break;

            case ERROR: {
                ErrorMessage tmp = Utils.tryCast(ErrorMessage.class, message);
                assert tmp != null;
                logger.print(tmp.getMessage());
            }
            break;

            default:
                // This should not happen, if it does it's a programmer error!
                // Fix this by sending back a message that is expected
                System.err.println("Server sends an incorrect response message for a fetch request message");
                assert false;
                break;
        }
    }
}
