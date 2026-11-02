package newcommon.items;

import newcommon.Entity;

public abstract class Item extends Entity {

    private int x;
    private int y;
    private int w;
    private int h;
    private int count;
    private int durability;

    public Item(long id, String name, int x, int y, int w, int h, int count, int durability) {
        super(id, name);
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.count = count;
        this.durability = durability;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean onItem(int x, int y) {
        if (x >= this.x && x <= this.x + this.w
                && y >= this.y && y <= this.y + this.h) {

            return true;
        }

        return false;
    }
}
