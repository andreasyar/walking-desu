package client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class NukeSpriteSet {
    private static HashMap<String, NukeSpriteSet> cache = new HashMap<String, NukeSpriteSet>();
    private ArrayList<BufferedImage> sprites = new ArrayList<BufferedImage>();
    private Sprite curSpr = new Sprite();

    public static NukeSpriteSet load(String name) {
        if (cache.containsKey(name)) {
            return (NukeSpriteSet) cache.get(name);
        } else {
            NukeSpriteSet set = new NukeSpriteSet(name);
            cache.put(name, set);
            return set;
        }
    }

    private NukeSpriteSet(String name) {
        if ("power_of_watering_pot".equals(name)) {
            loadSprFiles(new String[] {"img/" + name + "_01.png"});
        }
    }

    public Sprite getSprite(int index) {
        curSpr.image = sprites.get(index);
        return curSpr;
    }

    public int getSpriteCount() {
        return sprites.size();
    }

    private void loadSprFiles(String[] paths) {
        ClassLoader cl = this.getClass().getClassLoader();
        URL url = null;

        for (int i = 0; i < paths.length; i++) {
            try {
                url = cl.getResource(paths[i]);
                if (url != null) {
                    sprites.add(ImageIO.read(url));
                } else {
                    sprites.add(ImageIO.read(new File(paths[i])));
                }
            } catch (IOException e) {
                System.err.println(paths[i]);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
