package mailer;

import java.io.Serializable;
import java.util.Objects;

import mailer.exceptions.InvalidMailAddressException;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * MailAddress represents a mail address
 */
public final class MailAddress implements Serializable {

    private static final String AT = "@";

    private final String local;
    private final String domain;

    public static boolean validate(String mailAddress) {
        return EmailValidator.getInstance().isValid(mailAddress);
    }


    /**
     * Constructs a newly allocated MailAddress object from a String representation
     * of a mail address
     * @param mailAddress mail address string
     * @throws InvalidMailAddressException thrown when mail address string is invalid
     */
    public MailAddress(String mailAddress) throws InvalidMailAddressException {
        boolean isValid = validate(mailAddress);
        if (!isValid) {
            throw new InvalidMailAddressException(mailAddress);
        }

        // If mailAddress is a valid mail address then splitting
        // it will result in a String array with two elements
        String[] tmp = mailAddress.split(AT);
        local = tmp[0];
        domain = tmp[1];
    }

    /**
     * Constructs a newly allocated MailAddress object from a local and a domain String
     * @param local mail address's local part
     * @param domain mail address's domain part
     */
    public MailAddress(String local, String domain) {
        this.local = local;
        this.domain = domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailAddress that = (MailAddress) o;
        return Objects.equals(local, that.local) && Objects.equals(domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(local, domain);
    }

    @Override
    public String toString() {
        return local + AT + domain;
    }
}
