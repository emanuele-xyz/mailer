package mailer;

import java.io.Serializable;

public final class MailAddress implements Serializable {

    private final String localPart;
    private final String domain;

    public MailAddress(String localPart, String domain) {
        this.localPart = localPart;
        this.domain = domain;
    }

    public String getLocalPart() {
        return localPart;
    }

    public String getDomain() {
        return domain;
    }
}
