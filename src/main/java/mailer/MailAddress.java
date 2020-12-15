package mailer;

import java.util.Objects;
import org.apache.commons.validator.routines.EmailValidator;

public final class MailAddress {

    private static final String AT = "@";

    private final String local;
    private final String domain;

    public static boolean validate(String mailAddress) {
        return EmailValidator.getInstance().isValid(mailAddress);
    }

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
