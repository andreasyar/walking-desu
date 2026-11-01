package common;

/** Rejected inventory operation; the inventory is unchanged. */
public class InventoryException extends IllegalArgumentException {
    public InventoryException(String message) { super(message); }
}
