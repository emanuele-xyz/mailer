package client.tasks;

import client.ServerDispatcher;
import client.logger.Logger;

/**
 * Task is the superclass of all tasks. It's used to avoid code repetition
 */
public abstract class Task implements Runnable {

    protected static final int MESSAGE_WAIT_TIME = 10 * 1000;

    protected final ServerDispatcher serverDispatcher;
    protected final Logger logger;

    public Task(ServerDispatcher serverDispatcher, Logger logger) {
        this.serverDispatcher = serverDispatcher;
        this.logger = logger;
    }
}
