package client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class DirectionalSpriteSet {
    private static HashMap<String, DirectionalSpriteSet> cache = new HashMap<String, DirectionalSpriteSet>();
    private HashMap<Direction, ArrayList<BufferedImage>> sprites = new HashMap<Direction, ArrayList<BufferedImage>>();
    private Sprite curSpr = new Sprite();

    public static DirectionalSpriteSet load(String name) {
        if (cache.containsKey(name)) {
            return (DirectionalSpriteSet) cache.get(name);
        } else {
            DirectionalSpriteSet set = new DirectionalSpriteSet(name);
            cache.put(name, set);
            return set;
        }
    }

    private DirectionalSpriteSet(String name) {
        for (Direction d:Direction.values()) {
            sprites.put(d, new ArrayList<BufferedImage>());
        }

        // TODO WTF IT THIS?
        if (name.equals("desu_walk")) {
            loadSprFiles(new String[] {"img/" + name + "/north_01.png", "img/" + name + "/north_02.png"});
            loadSprFiles(new String[] {"img/" + name + "/north_east_01.png", "img/" + name + "/north_east_02.png"});
            loadSprFiles(new String[] {"img/" + name + "/north_west_01.png", "img/" + name + "/north_west_02.png"});
            loadSprFiles(new String[] {"img/" + name + "/south_04.png", "img/" + name + "/south_05.png"});
            loadSprFiles(new String[] {"img/" + name + "/south_east_01.png", "img/" + name + "/south_east_02.png"});
            loadSprFiles(new String[] {"img/" + name + "/south_west_01.png", "img/" + name + "/south_west_02.png"});
        }

        if (name.equals("desu_stand")) {
            loadSprFiles(new String[] {"img/" + name + "/north_01.png"});
            loadSprFiles(new String[] {"img/" + name + "/north_east_03.png"});
            loadSprFiles(new String[] {"img/" + name + "/north_west_03.png"});
            loadSprFiles(new String[] {"img/" + name + "/south_01.png", "img/" + name + "/south_02.png", "img/" + name + "/south_03.png", "img/" + name + "/south_04.png"});
            loadSprFiles(new String[] {"img/" + name + "/south_east_03.png"});
            loadSprFiles(new String[] {"img/" + name + "/south_west_03.png"});
        }
    }

    public Sprite getSprite(Direction d, int index) {
        curSpr.image = sprites.get(d).get(index);
        return curSpr;
    }

    public int getSpriteCount(Direction d) {
        return sprites.get(d).size();
    }

    private void loadSprFiles(String[] paths) {
        ClassLoader cl = this.getClass().getClassLoader();
        URL url = null;

        for (int i = 0; i < paths.length; i++) {
            try {
                url = cl.getResource(paths[i]);
                if (url != null) {
                    sprites.get(Direction.NORTH).add(ImageIO.read(url));
                } else {
                    sprites.get(Direction.NORTH).add(ImageIO.read(new File(paths[i])));
                }
            } catch (IOException e) {
                System.err.println(paths[i]);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
