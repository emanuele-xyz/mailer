package mailer;

/**
 * Constants collects different application wide constants
 */
public final class Constants {

    /**
     * Server port
     */
    public static final int SERVER_PORT = 8189;

    /**
     * Mailer server directory
     */
    public static final String SERVER_DIRECTORY = "mailerServer";

    /**
     * Number of system cores.
     * If hyper-threading is enabled, is fine to use <code>2 * CORE</code> threads to fully utilize the machine
     */
    public static final int CORES = Runtime.getRuntime().availableProcessors();

    private Constants() {
    }
}
