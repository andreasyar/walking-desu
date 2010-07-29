package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class WanderingMap {
    private static WanderingMap map = null;
    private static BufferedImage mapImg;

    private static final ArrayList<WanderingPolygon> geoData = new ArrayList<WanderingPolygon>();

    private WanderingMap(int w, int h) {
        Graphics g;

		mapImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		g = mapImg.getGraphics();
		g.setColor(Color.gray);
		g.fillRect(1, 1, w - 2, h - 2);
        generateGeoData();
    }

    public static BufferedImage getMapImg() {
        if (map == null) {
            map = new WanderingMap(1024, 768);
        }

        return mapImg;
    }

    private static void generateGeoData() {
        WanderingPolygon p = new WanderingPolygon(WanderingPolygon.WallType.MONOLITH) {

            @Override
            public void trigger(Player player) {
            }
        };
        p.addPoint(200, 200);
        p.addPoint(400, 200);
        p.addPoint(400, 400);
        p.addPoint(100, 400);
        p.addPoint(100, 300);
        p.addPoint(200, 300);
        geoData.add(p);
        p = new WanderingPolygon(WanderingPolygon.WallType.TRANSPARENT) {

            @Override
            public void trigger(Player player) {
            }
        };
        p.addPoint(100, 500);
        p.addPoint(200, 500);
        p.addPoint(200, 600);
        p.addPoint(100, 600);
        geoData.add(p);
        p = new WanderingPolygon(WanderingPolygon.WallType.SPECIAL) {

            @Override
            public void trigger(Player player) {
                player.teleportToSpawn();
            }
        };
        p.addPoint(500, 700);
        p.addPoint(600, 700);
        p.addPoint(600, 767);
        p.addPoint(500, 767);
        geoData.add(p);
    }

    public static ArrayList<WanderingPolygon> getGeoData() {
        if (map == null) {
            map = new WanderingMap(1024, 768);
        }

        return geoData;
    }
}
