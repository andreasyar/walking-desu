package server.javatestserver;

import java.util.ArrayList;

public class WanderingMap {
    private static WanderingMap map = null;

    private static final ArrayList<WanderingPolygon> geoData = new ArrayList<WanderingPolygon>();

    private WanderingMap() {
        generateGeoData();
    }

    private static void generateGeoData() {
        WanderingPolygon p = new WanderingPolygon(WanderingPolygon.WallType.MONOLITH) {

            @Override
            public void trigger(Player player) {
            }
        };
        p.addPoint(95, 223);
        p.addPoint(159, 223);
        p.addPoint(159, 127);
        p.addPoint(352, 127);
        p.addPoint(352, 352);
        p.addPoint(95, 352);
        geoData.add(p);
        p = new WanderingPolygon(WanderingPolygon.WallType.MONOLITH) {

            @Override
            public void trigger(Player player) {
            }
        };
        p.addPoint(0, 127);
        p.addPoint(32, 127);
        p.addPoint(32, 256);
        p.addPoint(0, 256);
        geoData.add(p);
        p = new WanderingPolygon(WanderingPolygon.WallType.MONOLITH) {

            @Override
            public void trigger(Player player) {
            }
        };
        p.addPoint(0, 351);
        p.addPoint(32, 351);
        p.addPoint(32, 735);
        p.addPoint(639, 735);
        p.addPoint(639, 703);
        p.addPoint(672, 703);
        p.addPoint(672, 768);
        p.addPoint(0, 768);
        geoData.add(p);
        p = new WanderingPolygon(WanderingPolygon.WallType.MONOLITH) {

            @Override
            public void trigger(Player player) {
            }
        };
        p.addPoint(735, 703);
        p.addPoint(768, 703);
        p.addPoint(768, 735);
        p.addPoint(991, 735);
        p.addPoint(991, 32);
        p.addPoint(864, 32);
        p.addPoint(864, 64);
        p.addPoint(832, 64);
        p.addPoint(832, 0);
        p.addPoint(1024, 0);
        p.addPoint(1024, 768);
        p.addPoint(735, 768);
        geoData.add(p);
        p = new WanderingPolygon(WanderingPolygon.WallType.MONOLITH) {

            @Override
            public void trigger(Player player) {
            }
        };
        p.addPoint(351, 0);
        p.addPoint(544, 0);
        p.addPoint(544, 64);
        p.addPoint(512, 64);
        p.addPoint(512, 32);
        p.addPoint(351, 32);
        geoData.add(p);
        p = new WanderingPolygon(WanderingPolygon.WallType.TRANSPARENT) {

            @Override
            public void trigger(Player player) {
            }
        };
        p.addPoint(287, 570);
        p.addPoint(189, 478);
        p.addPoint(195, 419);
        p.addPoint(248, 419);
        p.addPoint(289, 451);
        p.addPoint(351, 448);
        p.addPoint(351, 570);
        geoData.add(p);
        p = new WanderingPolygon(WanderingPolygon.WallType.SPECIAL) {

            @Override
            public void trigger(Player player) {
                player.teleportToSpawn();
            }
        };
        p.addPoint(545, 703);
        p.addPoint(545, 645);
        p.addPoint(606, 645);
        p.addPoint(606, 703);
        geoData.add(p);
        p = new WanderingPolygon(WanderingPolygon.WallType.SPECIAL) {

            @Override
            public void trigger(Player player) {
                player.teleportTo(2000, 2000);
            }
        };
        p.addPoint(487, 695);
        p.addPoint(487, 644);
        p.addPoint(539, 644);
        p.addPoint(539, 695);
        geoData.add(p);
    }

    public static ArrayList<WanderingPolygon> getGeoData() {
        if (map == null) {
            map = new WanderingMap();
        }

        return geoData;
    }
}
