package wand6.server.messages;

import wand6.common.messages.Message;
import wand6.common.messages.MessageType;

public record AppearEntityMessage(long id, wand6.common.Unit.Kind kind, String name, String spriteSetName, MoveMessage movement, double speed, int hp, int maxHp, AttackMessage attackAnimation) implements Message, java.io.Serializable {
    private static final long serialVersionUID = 11L;
    @Override public MessageType getType() { return MessageType.APPEARENTITY; }
}
