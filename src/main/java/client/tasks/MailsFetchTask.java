package client.tasks;

import client.Logger;
import client.ServerDispatcher;
import mailer.Mail;
import mailer.Utils;
import mailer.messages.ErrorMessage;
import mailer.messages.MailFetchRequestMessage;
import mailer.messages.MailFetchResponseMessage;
import mailer.messages.Message;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class MailsFetchTask implements Runnable {

    private static final int MESSAGE_WAIT_TIME = 10 * 1000;

    private static final AtomicBoolean isFetching = new AtomicBoolean(false);

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
        // If there already is another mail fetch task running, then close this task
        if (isFetching.get()) {
            return;
        }

        // Start fetching
        // Remember that we have to set it back to false when we finish fetching
        // to let other mail fetch tasks to run
        isFetching.set(true);

        Future<Message> message = serverDispatcher.sendToServer(new MailFetchRequestMessage(address), MESSAGE_WAIT_TIME);
        Message response = Utils.getResult(message);
        if (response == null) {
            logger.print("Error fetching mails from server! Try again");
            isFetching.set(false);
            return;
        }

        switch (response.getType()) {
            case FETCH_RESPONSE: {
                MailFetchResponseMessage tmp = Utils.tryCast(MailFetchResponseMessage.class, response);
                assert tmp != null;
                // If tmp where null it means that there is a mismatch between message class
                // and message type. This is a bug. We have to fix it in MailFetchResponseMessage class.

                List<Mail> mails = Arrays.stream(tmp.getMails())
                        .sorted(Comparator.comparing(Mail::getDate))
                        .collect(Collectors.toList());

                for (Mail mail : mails) {
                    onMailReceived.accept(mail);
                }
            }
            break;

            case ERROR: {
                ErrorMessage tmp = Utils.tryCast(ErrorMessage.class, response);
                assert tmp != null;
                // If tmp where null it means that there is a mismatch between message class
                // and message type. This is a bug. We have to fix it in ErrorMessage class.

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

        isFetching.set(false);
    }
}
