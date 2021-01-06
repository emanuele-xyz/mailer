package client.tasks;

import client.ServerDispatcher;
import client.logger.Logger;

public abstract class Task {

    private static final int MESSAGE_WAIT_TIME = 10 * 1000;

    protected final ServerDispatcher serverDispatcher;
    protected final Logger logger;

    public Task(ServerDispatcher serverDispatcher, Logger logger) {
        this.serverDispatcher = serverDispatcher;
        this.logger = logger;
    }
}
