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
        if (name.startsWith("poring_") || name.equals("tower_walk") || name.equals("tower_stand") || name.equals("canon_bolt") || name.equals("canon_hit")) {
            loadNpc(name); return;
        }
        if (!java.util.Set.of("peasant_walk", "peasant_stand", "peasant_attack", "peasant_death", "peasant_bolt").contains(name)) {
            throw new IllegalArgumentException("Unsupported wand6 sprite set: " + name);
        }
        String[] directions = {"e", "ne", "n", "nw", "w", "sw", "s", "se"};
        int count = name.endsWith("_walk") ? 4 : name.endsWith("_attack") ? 5 : name.endsWith("_death") ? 3 : 1;
        for (Direction direction : Direction.values()) {
            var frames = new ArrayList<BufferedImage>();
            for (int frame = 1; frame <= count; frame++) {
                String fileDirection = directions[direction.ordinal()];
                if (name.endsWith("_death")) fileDirection = switch (direction) {
                    case EAST, NORTH_EAST, NORTH -> "ne";
                    case NORTH_WEST, WEST -> "nw";
                    case SOUTH_WEST, SOUTH -> "sw";
                    case SOUTH_EAST -> "se";
                };
                String path = "img/" + name + "/" + fileDirection + "_0" + frame + ".png";
                BufferedImage image = ClientAssets.image(path);
                if (image.getWidth() < 1 || image.getHeight() < 1 || ((name.endsWith("_walk") || name.endsWith("_stand")) && (image.getWidth() < 64 || image.getHeight() < 64))) {
                    throw new IllegalArgumentException("Sprite must be at least 64x64: " + path);
                }
                frames.add(name.endsWith("_walk") || name.endsWith("_stand") ? image.getSubimage(8, 8, 56, 56) : image);
            }
            sprites.put(direction, frames);
        }
    }
    private void loadNpc(String name) {
        for (Direction direction : Direction.values()) {
            boolean east = direction == Direction.EAST || direction == Direction.NORTH_EAST
                    || direction == Direction.NORTH || direction == Direction.SOUTH_EAST;
            int count = name.equals("poring_death") ? 5 : name.startsWith("poring_") ? 4 : name.equals("canon_bolt") ? 3 : name.equals("canon_hit") ? 4 : 1;
            var frames = new ArrayList<BufferedImage>();
            for (int frame = 1; frame <= count; frame++) {
                String path = name.startsWith("tower_") ? "img/" + name + "/tower.png"
                        : name.equals("canon_bolt") ? "img/canon_bolt/" + new String[]{"e", "ne", "n", "nw", "w", "sw", "s", "se"}[direction.ordinal()] + "_0" + frame + ".png"
                        : name.equals("canon_hit") ? "img/canon_hit/e_0" + frame + ".png"
                        : "img/" + name + "/" + (name.endsWith("death") ? (east ? "e" : "w")
                        : (east ? "poring_east" : "poring_west")) + "_0" + frame + ".png";
                frames.add(ClientAssets.image(path));
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
