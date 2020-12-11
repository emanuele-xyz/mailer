package mailer;

import java.util.Objects;

public final class MailAddress {

    private static final String AT = "@";

    private final String local;
    private final String domain;

    // TODO: we should add better mail validation!
    public MailAddress(String address) throws InvalidMailAddressException {
        String[] tmp = address.split(AT);
        if (tmp.length != 2) {
            throw new InvalidMailAddressException(address);
        }

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
        return local + "@" + domain;
    }
}
