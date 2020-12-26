package mailer;

import com.google.gson.Gson;

public class MailJSONConverter {

    private static final Gson GSON = new Gson();

    public static Mail mailFromJson(String json) {
        return GSON.fromJson(json, Mail.class);
    }

    public static String mailToJson(Mail mail) {
        return GSON.toJson(mail);
    }
}
