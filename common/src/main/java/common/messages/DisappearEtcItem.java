package common.messages;

import common.Message;
import java.io.Serializable;

/** Visibility change only: never changes an inventory. Safe to apply repeatedly. */
public final class DisappearEtcItem implements Message, Serializable {
    private static final long serialVersionUID = 1L;
    private final long id;
    public DisappearEtcItem(long id) { this.id = id; }
    public long getId() { return id; }
    @Override public MessageType getType() { return MessageType.DISAPPEARETCITEM; }
    @Override public String toString() { return "Item id=" + id + " disappears from visibility."; }
}
