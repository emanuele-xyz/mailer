package client.models;

import javafx.beans.property.SimpleIntegerProperty;

/**
 * MainModelStateProperty is a custom property that models the main model's current state
 */
public final class MainModelStateProperty {

    // Here we define the possible states as integers instead of using an enumeration
    // This is done so that they can be easily stored into a SimpleIntegerProperty
    public static final int BLANK = 0;
    public static final int VIEWING = BLANK + 1;
    public static final int COMPOSING = VIEWING + 1;

    private static final int DEFAULT_STATE = BLANK;

    private final SimpleIntegerProperty stateIndex;

    public MainModelStateProperty() {
        stateIndex = new SimpleIntegerProperty(DEFAULT_STATE);
    }

    /**
     * Set current state to blank
     */
    public void setBlank() {
        stateIndex.set(BLANK);
    }

    /**
     * Set current state to viewing
     */
    public void setViewing() {
        stateIndex.set(VIEWING);
    }

    /**
     * Set current state to composing
     */
    public void setComposing() {
        stateIndex.set(COMPOSING);
    }

    public SimpleIntegerProperty stateIndexProperty() {
        return stateIndex;
    }
}
