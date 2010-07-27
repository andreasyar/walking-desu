package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class WanderingMap {
    private static WanderingMap map = null;
    private static BufferedImage mapImg;

    private static final ArrayList<Polygon> geoData = new ArrayList<Polygon>();

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
        Polygon p = new Polygon();
        p.addPoint(200, 200);
        p.addPoint(400, 200);
        p.addPoint(400, 400);
        p.addPoint(100, 400);
        p.addPoint(100, 300);
        p.addPoint(200, 300);
        geoData.add(p);
    }

    public static ArrayList<Polygon> getGeoData() {
        if (map == null) {
            map = new WanderingMap(1024, 768);
        }

        return geoData;
    }
}
