package mailer;

import java.io.IOException;
import java.io.ObjectInputStream;

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

    private Utils() {
    }
}
