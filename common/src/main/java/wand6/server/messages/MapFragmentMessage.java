package wand6.server.messages;

import wand6.common.messages.Message;
import wand6.common.messages.MessageType;
import java.io.Serializable;

public class MapFragmentMessage implements Message, Serializable {

    private final MessageType type = MessageType.MAPFRAGMENT;
    private int idX;
    private int idY;
    private int[][] hmap;

    public MapFragmentMessage(int idX, int idY, int[][] hmap) {
        this.idX = idX;
        this.idY = idY;
        this.hmap = hmap;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public int[][] getHmap() {
        return hmap;
    }

    public int getIdX() {
        return idX;
    }

    public int getIdY() {
        return idY;
    }

    @Override
    public String toString() {
        return "MapFragmentMessage{" + "idX=" + idX + " idY=" + idY + '}';
    }
}
