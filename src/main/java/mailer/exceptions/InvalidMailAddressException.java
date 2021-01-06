package mailer.exceptions;

public final class InvalidMailAddressException extends Throwable {
    public InvalidMailAddressException(String address) {
        super("Invalid mail address '" + address + "'");
    }
}
