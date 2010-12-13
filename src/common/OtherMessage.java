package common;

import common.messages.MessageType;
import java.io.Serializable;

public class OtherMessage implements Message, Serializable {

    private final MessageType type = MessageType.OTHER;
    private String message;

    public OtherMessage(String message) {
        this.message = message;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
