package mailer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Utils {

    // This never throws, if it fails, for whatever reason it returns null
    public static <T> T read(Class<T> target, ObjectInputStream in) {

        Object tmp = null;
        try {
            tmp = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return tryCast(target, tmp);
    }

    public static <T> T tryCast(Class<T> target, Object obj) {
        if (target.isInstance(obj)) {
            return target.cast(obj);
        } else {
            return null;
        }
    }

    public static <T> T cast(Class <T> target, Object obj) {
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

    public static <T> T getResult(Future<T> future, long timeout, TimeUnit timeUnit) {
        if (future == null) {
            return null;
        }

        T tmp = null;

        try {
            tmp = future.get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        return tmp;
    }

    private Utils() {
    }
}
