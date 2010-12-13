package common;
//
//import common.messages.MessageType;
//import java.io.Serializable;
//
//public class GoldCoinMessage implements Message, Serializable {
//
//    private final MessageType type = MessageType.GOLDCOIN;
//    private long id;
//    private int x;
//    private int y;
//    private int count;
//
//    public GoldCoinMessage(long id, int x, int y, int count) {
//        this.id = id;
//        this.x = x;
//        this.y = y;
//        this.count = count;
//    }
//
//    @Override
//    public MessageType getType() {
//        return type;
//    }
//
//    public long getId() {
//        return id;
//    }
//
//    public int getX() {
//        return x;
//    }
//
//    public int getY() {
//        return y;
//    }
//
//    public int getCount() {
//        return count;
//    }
//
//    @Override
//    public String toString() {
//        return id + " Gold coin(" + count + ") dropped at (" + x + ", " + y + ").";
//    }
//
//}
