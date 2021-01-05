package client.models;

import javafx.beans.property.SimpleIntegerProperty;

public final class MainModelStateProperty {

    public static final int BLANK = 0;
    public static final int VIEWING = BLANK + 1;
    public static final int COMPOSING = VIEWING + 1;

    private static final int DEFAULT_STATE = BLANK;

    private final SimpleIntegerProperty stateIndex;

    public MainModelStateProperty() {
        stateIndex = new SimpleIntegerProperty(DEFAULT_STATE);
    }

    public void setBlank() {
        stateIndex.set(BLANK);
    }

    public void setViewing() {
        stateIndex.set(VIEWING);
    }

    public void setComposing() {
        stateIndex.set(COMPOSING);
    }

    public SimpleIntegerProperty stateIndexProperty() {
        return stateIndex;
    }
}
