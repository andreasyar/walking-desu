package newcommon.items;

public abstract class Etc extends Item {

    private final Items type;

    public Etc(long id, String name, int x, int y, int w, int h, int count, int durability, Items type) {
        super(id, name, x, y, w, h, count, durability);
        this.type = type;
    }

    public Items getType() {
        return type;
    }
}
