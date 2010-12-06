package common;

import java.io.Serializable;

public class WPickupMessage implements Message, Serializable {

    private final MessageType type = MessageType.WPICKUP;
    private long id;

    public WPickupMessage(long id) {
        this.id = id;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Try to pick up " + id + ".";
    }

}
