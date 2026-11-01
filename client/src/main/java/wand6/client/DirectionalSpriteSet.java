package wand6.client;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** peasant walk/stand subset of 3.2 DirectionalSpriteSet, same paths and crop. */
public final class DirectionalSpriteSet {
    private static final Map<String, DirectionalSpriteSet> CACHE = new HashMap<>();
    private final Map<Direction, ArrayList<BufferedImage>> sprites = new EnumMap<>(Direction.class);
    public static synchronized DirectionalSpriteSet load(String name) {
        return CACHE.computeIfAbsent(name, DirectionalSpriteSet::new);
    }
    private DirectionalSpriteSet(String name) {
        if (!name.equals("peasant_walk") && !name.equals("peasant_stand")) {
            throw new IllegalArgumentException("Unsupported wand6 sprite set: " + name);
        }
        String[] directions = {"e", "ne", "n", "nw", "w", "sw", "s", "se"};
        int count = name.endsWith("_walk") ? 4 : 1;
        for (Direction direction : Direction.values()) {
            var frames = new ArrayList<BufferedImage>();
            for (int frame = 1; frame <= count; frame++) {
                String path = "img/" + name + "/" + directions[direction.ordinal()] + "_0" + frame + ".png";
                BufferedImage image = ClientAssets.image(path);
                if (image.getWidth() < 64 || image.getHeight() < 64) {
                    throw new IllegalArgumentException("Sprite must be at least 64x64: " + path);
                }
                frames.add(image.getSubimage(8, 8, 56, 56));
            }
            sprites.put(direction, frames);
        }
    }
    public Sprite getSprite(Direction direction, int index) {
        Sprite sprite = new Sprite();
        sprite.image = sprites.get(java.util.Objects.requireNonNull(direction)).get(index);
        return sprite;
    }
    public int getSpriteCount(Direction direction) { return sprites.get(direction).size(); }
}
