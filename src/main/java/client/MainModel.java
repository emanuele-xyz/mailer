package client;

public final class MainModel {

    private final ServerDispatcher serverDispatcher;

    public MainModel(ServerDispatcher serverDispatcher) {
        this.serverDispatcher = serverDispatcher;
    }

    public void close() {
        serverDispatcher.shutdown();
    }
}
