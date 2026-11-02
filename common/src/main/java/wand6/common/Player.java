package wand6.common;

/** A connected player; NPCs share Unit state without owning a connection. */
public final class Player extends Unit {
    private final Inventory inventory = new Inventory();
    public Inventory inventory() { return inventory; }
    public Player(long id, String name, String spriteSetName) { super(id, name, spriteSetName); }
}
