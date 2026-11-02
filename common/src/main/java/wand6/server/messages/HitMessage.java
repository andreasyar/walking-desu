package wand6.server.messages;

import java.io.Serializable;
import wand6.common.messages.Message;
import wand6.common.messages.MessageType;

public record HitMessage(long shotId, long attackerId, long targetId, int damage, int hp, long life, long time) implements Message, Serializable {
    private static final long serialVersionUID = 10L;
    @Override public MessageType getType() { return MessageType.HIT; }
}
