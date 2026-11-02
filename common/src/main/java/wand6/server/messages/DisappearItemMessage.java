package wand6.server.messages;

import wand6.common.messages.Message;
import wand6.common.messages.MessageType;

public record DisappearItemMessage(long id) implements Message, java.io.Serializable {
    private static final long serialVersionUID = 12L;
    @Override public MessageType getType() { return MessageType.DISAPPEARETCITEM; }
}
