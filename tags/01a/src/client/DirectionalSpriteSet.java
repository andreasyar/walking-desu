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
        for (Direction d : Direction.values()) {
            sprites.put(d, new ArrayList<BufferedImage>());
        }

        // TODO WTF IT THIS?
        if (name.equals("desu_walk")) {
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/north_01.png", "img/" + name + "/north_02.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/north_east_01.png", "img/" + name + "/north_east_02.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/north_west_01.png", "img/" + name + "/north_west_02.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/south_04.png", "img/" + name + "/south_05.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/south_east_01.png", "img/" + name + "/south_east_02.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/south_west_01.png", "img/" + name + "/south_west_02.png"});
        } else if (name.equals("desu_stand")) {
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/north_01.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/north_east_03.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/north_west_03.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/south_01.png", "img/" + name + "/south_02.png", "img/" + name + "/south_03.png", "img/" + name + "/south_02.png", "img/" + name + "/south_01.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/south_east_03.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/south_west_03.png"});
        } else if (name.equals("poring_walk")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/poring_east_01.png", "img/" + name + "/poring_east_02.png", "img/" + name + "/poring_east_03.png", "img/" + name + "/poring_east_04.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/poring_east_01.png", "img/" + name + "/poring_east_02.png", "img/" + name + "/poring_east_03.png", "img/" + name + "/poring_east_04.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/poring_east_01.png", "img/" + name + "/poring_east_02.png", "img/" + name + "/poring_east_03.png", "img/" + name + "/poring_east_04.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/poring_west_01.png", "img/" + name + "/poring_west_02.png", "img/" + name + "/poring_west_03.png", "img/" + name + "/poring_west_04.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/poring_west_01.png", "img/" + name + "/poring_west_02.png", "img/" + name + "/poring_west_03.png", "img/" + name + "/poring_west_04.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/poring_west_01.png", "img/" + name + "/poring_west_02.png", "img/" + name + "/poring_west_03.png", "img/" + name + "/poring_west_04.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/poring_west_01.png", "img/" + name + "/poring_west_02.png", "img/" + name + "/poring_west_03.png", "img/" + name + "/poring_west_04.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/poring_east_01.png", "img/" + name + "/poring_east_02.png", "img/" + name + "/poring_east_03.png", "img/" + name + "/poring_east_04.png"});
        } else if (name.equals("poring_stand")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/poring_east_01.png", "img/" + name + "/poring_east_02.png", "img/" + name + "/poring_east_03.png", "img/" + name + "/poring_east_04.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/poring_east_01.png", "img/" + name + "/poring_east_02.png", "img/" + name + "/poring_east_03.png", "img/" + name + "/poring_east_04.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/poring_east_01.png", "img/" + name + "/poring_east_02.png", "img/" + name + "/poring_east_03.png", "img/" + name + "/poring_east_04.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/poring_west_01.png", "img/" + name + "/poring_west_02.png", "img/" + name + "/poring_west_03.png", "img/" + name + "/poring_west_04.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/poring_west_01.png", "img/" + name + "/poring_west_02.png", "img/" + name + "/poring_west_03.png", "img/" + name + "/poring_west_04.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/poring_west_01.png", "img/" + name + "/poring_west_02.png", "img/" + name + "/poring_west_03.png", "img/" + name + "/poring_west_04.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/poring_west_01.png", "img/" + name + "/poring_west_02.png", "img/" + name + "/poring_west_03.png", "img/" + name + "/poring_west_04.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/poring_east_01.png", "img/" + name + "/poring_east_02.png", "img/" + name + "/poring_east_03.png", "img/" + name + "/poring_east_04.png"});
        } else if (name.equals("tower_walk")) {
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/tower.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/tower.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/tower.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/tower.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/tower.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/tower.png"});
        } else if (name.equals("tower_stand")) {
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/tower.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/tower.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/tower.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/tower.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/tower.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/tower.png"});
        } else if (name.equals("peasant_walk")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/ne_01.png", "img/" + name + "/ne_02.png", "img/" + name + "/ne_03.png", "img/" + name + "/ne_04.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/n_01.png", "img/" + name + "/n_02.png", "img/" + name + "/n_03.png", "img/" + name + "/n_04.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/nw_01.png", "img/" + name + "/nw_02.png", "img/" + name + "/nw_03.png", "img/" + name + "/nw_04.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/w_01.png", "img/" + name + "/w_02.png", "img/" + name + "/w_03.png", "img/" + name + "/w_04.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/sw_01.png", "img/" + name + "/sw_02.png", "img/" + name + "/sw_03.png", "img/" + name + "/sw_04.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/s_01.png", "img/" + name + "/s_02.png", "img/" + name + "/s_03.png", "img/" + name + "/s_04.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/se_01.png", "img/" + name + "/se_02.png", "img/" + name + "/se_03.png", "img/" + name + "/se_04.png"});
        } else if (name.equals("peasant_stand")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/e_01.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/ne_01.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/n_01.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/nw_01.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/w_01.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/sw_01.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/s_01.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/se_01.png"});
        } else if (name.equals("peon_walk")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/ne_01.png", "img/" + name + "/ne_02.png", "img/" + name + "/ne_03.png", "img/" + name + "/ne_04.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/n_01.png", "img/" + name + "/n_02.png", "img/" + name + "/n_03.png", "img/" + name + "/n_04.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/nw_01.png", "img/" + name + "/nw_02.png", "img/" + name + "/nw_03.png", "img/" + name + "/nw_04.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/w_01.png", "img/" + name + "/w_02.png", "img/" + name + "/w_03.png", "img/" + name + "/w_04.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/sw_01.png", "img/" + name + "/sw_02.png", "img/" + name + "/sw_03.png", "img/" + name + "/sw_04.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/s_01.png", "img/" + name + "/s_02.png", "img/" + name + "/s_03.png", "img/" + name + "/s_04.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/se_01.png", "img/" + name + "/se_02.png", "img/" + name + "/se_03.png", "img/" + name + "/se_04.png"});
        } else if (name.equals("peon_stand")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/e_01.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/ne_01.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/n_01.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/nw_01.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/w_01.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/sw_01.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/s_01.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/se_01.png"});
        } else if (name.equals("canon_bolt")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/ne_01.png", "img/" + name + "/ne_02.png", "img/" + name + "/ne_03.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/n_01.png", "img/" + name + "/n_02.png", "img/" + name + "/n_03.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/nw_01.png", "img/" + name + "/nw_02.png", "img/" + name + "/nw_03.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/w_01.png", "img/" + name + "/w_02.png", "img/" + name + "/w_03.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/sw_01.png", "img/" + name + "/sw_02.png", "img/" + name + "/sw_03.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/s_01.png", "img/" + name + "/s_02.png", "img/" + name + "/s_03.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/se_01.png", "img/" + name + "/se_02.png", "img/" + name + "/se_03.png"});
        } else if (name.equals("poring_death")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png", "img/" + name + "/e_05.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png", "img/" + name + "/e_05.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png", "img/" + name + "/e_05.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/w_01.png", "img/" + name + "/w_02.png", "img/" + name + "/w_03.png", "img/" + name + "/w_04.png", "img/" + name + "/w_05.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/w_01.png", "img/" + name + "/w_02.png", "img/" + name + "/w_03.png", "img/" + name + "/w_04.png", "img/" + name + "/w_05.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/w_01.png", "img/" + name + "/w_02.png", "img/" + name + "/w_03.png", "img/" + name + "/w_04.png", "img/" + name + "/w_05.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/w_01.png", "img/" + name + "/w_02.png", "img/" + name + "/w_03.png", "img/" + name + "/w_04.png", "img/" + name + "/w_05.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png", "img/" + name + "/e_05.png"});
        } else if (name.equals("canon_hit")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png"});
        } else if (name.equals("peasant_bolt")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/e_01.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/ne_01.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/n_01.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/nw_01.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/w_01.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/sw_01.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/s_01.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/se_01.png"});
        } else if (name.equals("peasant_attack")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png", "img/" + name + "/e_05.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/ne_01.png", "img/" + name + "/ne_02.png", "img/" + name + "/ne_03.png", "img/" + name + "/ne_04.png", "img/" + name + "/ne_05.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/n_01.png", "img/" + name + "/n_02.png", "img/" + name + "/n_03.png", "img/" + name + "/n_04.png", "img/" + name + "/n_05.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/nw_01.png", "img/" + name + "/nw_02.png", "img/" + name + "/nw_03.png", "img/" + name + "/nw_04.png", "img/" + name + "/nw_05.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/w_01.png", "img/" + name + "/w_02.png", "img/" + name + "/w_03.png", "img/" + name + "/w_04.png", "img/" + name + "/w_05.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/sw_01.png", "img/" + name + "/sw_02.png", "img/" + name + "/sw_03.png", "img/" + name + "/sw_04.png", "img/" + name + "/sw_05.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/s_01.png", "img/" + name + "/s_02.png", "img/" + name + "/s_03.png", "img/" + name + "/s_04.png", "img/" + name + "/s_05.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/se_01.png", "img/" + name + "/se_02.png", "img/" + name + "/se_03.png", "img/" + name + "/se_04.png", "img/" + name + "/se_05.png"});
        } else if (name.equals("peon_attack")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/e_01.png", "img/" + name + "/e_02.png", "img/" + name + "/e_03.png", "img/" + name + "/e_04.png", "img/" + name + "/e_05.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/ne_01.png", "img/" + name + "/ne_02.png", "img/" + name + "/ne_03.png", "img/" + name + "/ne_04.png", "img/" + name + "/ne_05.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/n_01.png", "img/" + name + "/n_02.png", "img/" + name + "/n_03.png", "img/" + name + "/n_04.png", "img/" + name + "/n_05.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/nw_01.png", "img/" + name + "/nw_02.png", "img/" + name + "/nw_03.png", "img/" + name + "/nw_04.png", "img/" + name + "/nw_05.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/w_01.png", "img/" + name + "/w_02.png", "img/" + name + "/w_03.png", "img/" + name + "/w_04.png", "img/" + name + "/w_05.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/sw_01.png", "img/" + name + "/sw_02.png", "img/" + name + "/sw_03.png", "img/" + name + "/sw_04.png", "img/" + name + "/sw_05.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/s_01.png", "img/" + name + "/s_02.png", "img/" + name + "/s_03.png", "img/" + name + "/s_04.png", "img/" + name + "/s_05.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/se_01.png", "img/" + name + "/se_02.png", "img/" + name + "/se_03.png", "img/" + name + "/se_04.png", "img/" + name + "/se_05.png"});
        } else if (name.equals("peasant_death")) {
            loadSprFiles(Direction.EAST, new String[] {"img/" + name + "/ne_01.png", "img/" + name + "/ne_02.png", "img/" + name + "/ne_03.png"});
            loadSprFiles(Direction.NORTH_EAST, new String[] {"img/" + name + "/ne_01.png", "img/" + name + "/ne_02.png", "img/" + name + "/ne_03.png"});
            loadSprFiles(Direction.NORTH, new String[] {"img/" + name + "/ne_01.png", "img/" + name + "/ne_02.png", "img/" + name + "/ne_03.png"});
            loadSprFiles(Direction.NORTH_WEST, new String[] {"img/" + name + "/nw_01.png", "img/" + name + "/nw_02.png", "img/" + name + "/nw_03.png"});
            loadSprFiles(Direction.WEST, new String[] {"img/" + name + "/nw_01.png", "img/" + name + "/nw_02.png", "img/" + name + "/nw_03.png"});
            loadSprFiles(Direction.SOUTH_WEST, new String[] {"img/" + name + "/sw_01.png", "img/" + name + "/sw_02.png", "img/" + name + "/sw_03.png"});
            loadSprFiles(Direction.SOUTH, new String[] {"img/" + name + "/sw_01.png", "img/" + name + "/sw_02.png", "img/" + name + "/sw_03.png"});
            loadSprFiles(Direction.SOUTH_EAST, new String[] {"img/" + name + "/se_01.png", "img/" + name + "/se_02.png", "img/" + name + "/se_03.png"});
        } else {
            System.err.println("Sprite set " + name + " not found");
            System.exit(1);
        }
    }

    public Sprite getSprite(Direction d, int index) {
        curSpr.image = sprites.get(d).get(index);
        return curSpr;
    }

    public int getSpriteCount(Direction d) {
        return sprites.get(d).size();
    }

    private void loadSprFiles(Direction d, String[] paths) {
        ClassLoader cl = this.getClass().getClassLoader();
        URL url = null;

        for (int i = 0; i < paths.length; i++) {
            try {
                url = cl.getResource(paths[i]);
                if (url != null) {
                    sprites.get(d).add(ImageIO.read(url));
                } else {
                    sprites.get(d).add(ImageIO.read(new File(paths[i])));
                }
            } catch (IOException e) {
                System.err.println(paths[i]);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
