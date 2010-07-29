package client;

import java.awt.Polygon;

public abstract class WanderingPolygon extends Polygon {
    private WallType type;

    public WanderingPolygon(WallType type) {
        super();
        this.type = type;
    }

    public WallType getType() {
        return type;
    }

    public abstract void trigger(Player player);

    public static enum WallType {
        MONOLITH,
        TRANSPARENT,
        SPECIAL;
    }
}
