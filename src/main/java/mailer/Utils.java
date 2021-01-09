package mailer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Utils collects utility functions
 */
public final class Utils {

    /**
     * Read an object of target type from an object input stream
     *
     * @param target target type
     * @param in     source
     * @return an object of type <code>target</code> if operation succeeded, null otherwise
     */
    public static <T> T read(Class<T> target, ObjectInputStream in) {

        Object tmp = null;
        try {
            tmp = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return tryCast(target, tmp);
    }

    /**
     * Try to cast an object to a target type
     *
     * @param target target type
     * @param obj    the object to be cast
     * @return the object after casting if the operation succeeds, null otherwise
     */
    public static <T> T tryCast(Class<T> target, Object obj) {
        if (target.isInstance(obj)) {
            return target.cast(obj);
        } else {
            return null;
        }
    }

    /**
     * Cast an object to a target type
     *
     * @param target the target type
     * @param obj    the object to be cast
     * @return the object after casting
     * @throws ClassCastException thrown if cast fails
     */
    public static <T> T cast(Class<T> target, Object obj) {
        return target.cast(obj);
    }

    public static <T> T getResult(Future<T> future) {
        if (future == null) {
            return null;
        }

        T tmp = null;

        try {
            tmp = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return tmp;
    }

    private Utils() {
    }
}
