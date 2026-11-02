package wand6.server.messages;

import java.io.Serializable;
import wand6.common.messages.Message;
import wand6.common.messages.MessageType;

public record DeathMessage(long id, long life, int x, int y, long time) implements Message, Serializable {
    private static final long serialVersionUID = 10L;
    @Override public MessageType getType() { return MessageType.DEATH; }
}
