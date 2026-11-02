package wand6.server.messages;

import java.io.Serializable;
import wand6.common.messages.Message;
import wand6.common.messages.MessageType;

public record AttackMessage(long shotId, long attackerId, long targetId, long time, int bx, int by, int ex, int ey) implements Message, Serializable {
    private static final long serialVersionUID = 10L;
    @Override public MessageType getType() { return MessageType.ATTACK; }
}
