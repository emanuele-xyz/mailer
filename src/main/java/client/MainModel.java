package client;

import java.net.UnknownHostException;

public final class MainModel {

    private final ServerDispatcher serverDispatcher;

    public MainModel() throws UnknownHostException {
        this.serverDispatcher = new ServerDispatcher();
    }

    public void close() {
        serverDispatcher.shutdown();
    }
}
