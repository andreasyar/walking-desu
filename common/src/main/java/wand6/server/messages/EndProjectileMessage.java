package wand6.server.messages;

import java.io.Serializable;
import wand6.common.messages.Message;
import wand6.common.messages.MessageType;

public record EndProjectileMessage(long shotId) implements Message, Serializable {
    private static final long serialVersionUID = 10L;
    @Override public MessageType getType() { return MessageType.ENDPROJECTILE; }
}
