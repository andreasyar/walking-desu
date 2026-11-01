package server.javatestserver;

import java.util.ArrayList;

final class GeoDataController extends WorldTask {
    private final ArrayList<WanderingPolygon> geoData = WanderingMap.getGeoData();
    GeoDataController(WorldState world) { super(world); }
    @Override void step() {
        for (WanderingPolygon polygon : geoData) {
            if (polygon.getType() == WanderingPolygon.WallType.SPECIAL) {
                for (Player player : world.players)
                    if (polygon.contains(player.getCurPos())) polygon.trigger(player);
            }
        }
    }
}
