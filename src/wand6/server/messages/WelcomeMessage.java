package wand6.server.messages;

import wand6.common.messages.Message;
import wand6.common.messages.MessageType;
import java.io.Serializable;

public class WelcomeMessage implements Message, Serializable {

    private final MessageType type = MessageType.WELCOME;
    private long id;
    private String name;
    private String spriteSetName;
    private int x;
    private int y;

    public WelcomeMessage(long id, String name, String spriteSetName, int x, int y) {
        this.id = id;
        this.name = name;
        this.spriteSetName = spriteSetName;
        this.x = x;
        this.y = y;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "WelcomeMessage{" + "id=" + id + "name=" + name + "spriteSetName=" + spriteSetName + "x=" + x + "y=" + y + '}';
    }
}
