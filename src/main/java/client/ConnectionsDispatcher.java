package client;

import mailer.Constants;
import mailer.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ConnectionsDispatcher {

    private final String hostname;
    private final ExecutorService exec;

    public ConnectionsDispatcher() throws UnknownHostException {
        hostname = InetAddress.getLocalHost().getHostName();
        exec = Executors.newFixedThreadPool(Constants.CORES);
    }

    public void shutdown() {
        exec.shutdown();
    }

    // TODO: report operation failure
    public void sendToServer(Message message) {
        try  {
            Socket socket = new Socket(hostname, Constants.SERVER_PORT);

            try {
                // The socket will be closed in any case by our handler
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                Runnable task = new ServerHandler(socket, in, out, message);
                exec.submit(task);
            } catch (IOException e) {
                // Input or output stream creation
                e.printStackTrace();
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
