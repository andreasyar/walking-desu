package wand6.client.messages;

import wand6.common.messages.MessageType;
import java.io.Serializable;
import wand6.common.messages.Message;

public class TextCloudMessage implements Message, Serializable {

    private final MessageType type = MessageType.TEXTCLOUD;
    private String text;

    public TextCloudMessage(String text) {
        this.text = text;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "TextCloudMessage{" + "text=" + text + '}';
    }
}
