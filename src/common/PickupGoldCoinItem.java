package common;
//
//import common.messages.MessageType;
//import java.io.Serializable;
//
///**
// *
// * @author sorc
// */
//public class PickupGoldCoinItem implements Message, Serializable {
//
//    private final MessageType type = MessageType.PICKUPGOLDCOIN;
//    private long id;
//    private int count;
//
//    public PickupGoldCoinItem(long id, int count) {
//        this.id = id;
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
//    public int getCount() {
//        return count;
//    }
//
//    @Override
//    public String toString() {
//        return id + " Gold coin(" + count + ") added to inventory.";
//    }
//
//}