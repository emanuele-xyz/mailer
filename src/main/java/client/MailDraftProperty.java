package client;

import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

public final class MailDraftProperty {

    private final SimpleStringProperty subject;
    private final List<SimpleStringProperty> tos;
    private final SimpleStringProperty text;

    public MailDraftProperty() {
        subject = new SimpleStringProperty();
        tos = new ArrayList<>();
        text = new SimpleStringProperty();
    }

    public SimpleStringProperty addRecipient() {
        SimpleStringProperty tmp = new SimpleStringProperty();
        tos.add(tmp);
        return tmp;
    }

    public void removeRecipient(SimpleStringProperty prop) {
        tos.remove(prop);
    }

    public SimpleStringProperty subjectProperty() {
        return subject;
    }

    public List<SimpleStringProperty> getTos() {
        return tos;
    }

    public SimpleStringProperty textProperty() {
        return text;
    }
}
