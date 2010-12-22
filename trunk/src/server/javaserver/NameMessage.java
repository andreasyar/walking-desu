package server.javaserver;

import java.io.Serializable;

public class NameMessage implements Message, Serializable {

    private final MessageTypes type = MessageTypes.NAME;
    private String name;

    public NameMessage(String name) {
        this.name = name;
    }

    @Override
    public MessageTypes getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
