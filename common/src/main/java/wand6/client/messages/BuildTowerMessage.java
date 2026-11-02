package wand6.client.messages;

import wand6.common.messages.Message;
import wand6.common.messages.MessageType;

public record BuildTowerMessage(int x, int y) implements Message, java.io.Serializable {
    private static final long serialVersionUID = 11L;
    @Override public MessageType getType() { return MessageType.BUILDTOWER; }
}
