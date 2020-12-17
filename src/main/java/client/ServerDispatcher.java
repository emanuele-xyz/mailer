package client;

import mailer.Constants;
import mailer.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ServerDispatcher {

    private final String hostname;
    private final ExecutorService exec;

    public ServerDispatcher() throws UnknownHostException {
        hostname = InetAddress.getLocalHost().getHostName();
        exec = Executors.newFixedThreadPool(Constants.CORES);
    }

    public void shutdown() {
        exec.shutdown();
    }

    // TODO: report operation failure
    public Future<Message> sendToServer(Message message) {
        Future<Message> result = null;

        try  {
            Socket socket = new Socket(hostname, Constants.SERVER_PORT);

            try {
                // The socket will be closed in any case by our handler
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                Callable<Message> task = new ServerHandler(socket, in, out, message);
                result = exec.submit(task);
            } catch (IOException e) {
                // Input or output stream creation
                e.printStackTrace();
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
