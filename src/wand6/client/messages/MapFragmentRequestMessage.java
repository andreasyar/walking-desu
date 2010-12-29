package wand6.client.messages;

import wand6.common.messages.MessageType;
import java.io.Serializable;
import wand6.common.messages.Message;

public class MapFragmentRequestMessage implements Message, Serializable {

    private final MessageType type = MessageType.MAPFRAGMENTREQEST;
    private int idX;
    private int idY;

    public MapFragmentRequestMessage(int idX, int idY) {
        this.idX = idX;
        this.idY = idY;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public int getIdX() {
        return idX;
    }

    public int getIdY() {
        return idY;
    }

    @Override
    public String toString() {
        return "MapFragmentRequestMessage{" + "idX=" + idX + " idY=" + idY + '}';
    }
}
