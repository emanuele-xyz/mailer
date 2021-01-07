package client.tasks;

import client.ServerDispatcher;
import client.logger.Logger;
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
import java.util.stream.Collectors;

public final class MailsFetchTask extends Task {

    private static final AtomicBoolean isFetching = new AtomicBoolean(false);

    private final String address;
    private final MailFetchCallback onMailsReceived;

    public MailsFetchTask(ServerDispatcher serverDispatcher, Logger logger, String address, MailFetchCallback onMailsReceived) {
        super(serverDispatcher, logger);
        this.address = address;
        this.onMailsReceived = onMailsReceived;
    }

    @Override
    public void run() {
        // If there already is another mail fetch task running, then close this task
        if (isFetching.get()) {
            return;
        }

        // Start fetching
        // Remember that we have to set it back to false when we finish fetching
        // to allow other mail fetch tasks to run
        isFetching.set(true);

        Future<Message> message = serverDispatcher.sendToServer(new MailFetchRequestMessage(address), MESSAGE_WAIT_TIME);
        Message response = Utils.getResult(message);
        if (response == null) {
            // Silence fetching errors since is a background task
            // logger.error("Error fetching mails from server! Try again");
            isFetching.set(false);
            return;
        }

        switch (response.getType()) {
            case FETCH_RESPONSE: {
                MailFetchResponseMessage tmp = Utils.cast(MailFetchResponseMessage.class, response);

                List<Mail> receivedMails = Arrays.stream(tmp.getMails())
                        .sorted(Comparator.comparing(Mail::getDate))
                        .collect(Collectors.toList());

                onMailsReceived.exec(receivedMails);
            }
            break;

            case ERROR: {
                ErrorMessage tmp = Utils.cast(ErrorMessage.class, response);
                logger.error(tmp.getMessage());
            }
            break;

            default:
                // This should not happen, if it does it's a programmer error!
                System.err.printf("Received unexpected '%s' message in response to mail fetch message\n", response.getType());
                assert false;
                break;
        }

        // IMPORTANT!
        isFetching.set(false);
    }
}
