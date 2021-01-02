package server.exceptions;

import java.util.UUID;

public final class InvalidIDException extends Exception {

    private final UUID id;

    public InvalidIDException(UUID id) {
        super("Invalid mail ID");
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
