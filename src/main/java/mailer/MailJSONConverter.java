package mailer;

import com.google.gson.Gson;

/**
 * MailJSONConverter handles mail JSON conversion
 */
public class MailJSONConverter {

    private static final Gson GSON = new Gson();

    /**
     * Build a mail object from JSON data
     *
     * @param json json data
     * @return mail built from JSON data
     */
    public static Mail mailFromJson(String json) {
        return GSON.fromJson(json, Mail.class);
    }

    /**
     * Converts a mail into a JSON data
     *
     * @param mail the mail to convert
     * @return mail JSON data
     */
    public static String mailToJson(Mail mail) {
        return GSON.toJson(mail);
    }
}
