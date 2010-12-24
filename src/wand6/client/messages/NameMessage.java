package wand6.client.messages;

import wand6.common.messages.MessageType;
import java.io.Serializable;
import wand6.common.messages.Message;

public class NameMessage implements Message, Serializable {

    private final MessageType type = MessageType.NAME;
    private String name;

    public NameMessage(String name) {
        this.name = name;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "NameMessage{" + "name=" + name + '}';
    }
}
