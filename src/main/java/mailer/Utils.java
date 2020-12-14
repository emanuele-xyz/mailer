package mailer;

import java.io.IOException;
import java.io.ObjectInputStream;

public final class Utils {

    public static <T> T read(Class<T> target, ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object tmp = in.readObject();
        if (tmp != null && tmp.getClass().equals(target)) {
            return (T) tmp;
        } else {
            return null;
        }
    }

    private Utils() {
    }
}
