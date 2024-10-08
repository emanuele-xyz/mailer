package client;

import mailer.Constants;
import mailer.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ServerDispatcher dispatches messages to the server
 */
public final class ServerDispatcher {

    private final String hostname;
    private final ExecutorService exec;

    /**
     * Initializes a newly allocated ServerDispatcher object
     *
     * @throws UnknownHostException thrown if the local host name could not be
     *                              resolved into an address
     */
    public ServerDispatcher() throws UnknownHostException {
        hostname = InetAddress.getLocalHost().getHostName();
        exec = Executors.newFixedThreadPool(Constants.CORES);
    }

    /**
     * Shutdown the server dispatcher.
     * This will be called from other threads
     */
    public void shutdown() {
        synchronized (exec) {
            exec.shutdown();
        }
    }

    /**
     * Send a message to a server
     *
     * @param message      the message to be sent
     * @param timeoutAfter socket timeout time
     * @return a future to the server response message or null if something went wrong
     */
    public Future<Message> sendToServer(Message message, int timeoutAfter) {
        Future<Message> result = null;

        try {
            Socket socket = new Socket(hostname, Constants.SERVER_PORT);
            socket.setSoTimeout(timeoutAfter);

            try {
                // The socket will be closed by our handler if stream creation is successful
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                Callable<Message> task = new ServerHandler(socket, in, out, message);

                synchronized (exec) {
                    result = exec.submit(task);
                }

            } catch (IOException e) {
                // Input or output stream creation failed

                try {
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                e.printStackTrace();
            }

        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
