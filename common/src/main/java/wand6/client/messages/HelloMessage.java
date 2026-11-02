package wand6.client.messages;

import wand6.common.messages.MessageType;
import java.io.Serializable;
import wand6.common.messages.Message;

public class HelloMessage implements Message, Serializable {

    private final MessageType type = MessageType.HELLO;

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "HelloMessage{" + '}';
    }
}
