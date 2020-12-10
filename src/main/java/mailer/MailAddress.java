package mailer;

public final class MailAddress {

    private final String localPart;
    private final String domain;

    public MailAddress(String localPart, String domain) {
        this.localPart = localPart;
        this.domain = domain;
    }

    @Override
    public String toString() {
        return localPart + "@" + domain;
    }
}
