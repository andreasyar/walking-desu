package server.javaserver;

import java.io.Serializable;

class WelcomeMessage implements Message, Serializable {

    private final MessageTypes type = MessageTypes.WELCOME;
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
    public MessageTypes getType() {
        return type;
    }
}
