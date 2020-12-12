package server;

public class NoSuchAddressException extends Throwable {
    public NoSuchAddressException(String message) {
        super(message);
    }
}
