package wand6.server.messages;

import java.io.Serializable;
import wand6.common.messages.Message;
import wand6.common.messages.MessageType;

public record AttackMessage(long shotId, long attackerId, long targetId, long time, int bx, int by, int ex, int ey, long windup) implements Message, Serializable {
    private static final long serialVersionUID = 11L;
    public AttackMessage(long shotId, long attackerId, long targetId, long time, int bx, int by, int ex, int ey) {
        this(shotId, attackerId, targetId, time, bx, by, ex, ey, wand6.common.CombatRules.WINDUP);
    }
    @Override public MessageType getType() { return MessageType.ATTACK; }
}
