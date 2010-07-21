package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class WanderingMap {
    private static WanderingMap map = null;
    private static BufferedImage mapImg;

    private WanderingMap(int w, int h) {
        Graphics g;

		mapImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		g = mapImg.getGraphics();
		g.setColor(Color.gray);
		g.fillRect(1, 1, w - 2, h - 2);
    }

    public static BufferedImage getMapImg() {
        if (map == null) {
            map = new WanderingMap(1024, 768);
        }

        return mapImg;
    }
}
