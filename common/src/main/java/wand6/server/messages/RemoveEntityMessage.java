package wand6.server.messages;

import wand6.common.messages.Message;
import wand6.common.messages.MessageType;

public record RemoveEntityMessage(long id, boolean killed) implements Message, java.io.Serializable {
    private static final long serialVersionUID = 11L;
    @Override public MessageType getType() { return MessageType.REMOVEENTITY; }
}
