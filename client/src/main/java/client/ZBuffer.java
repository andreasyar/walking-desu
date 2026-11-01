package client;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Comparator;

/** Stable depth order, including sprites whose world anchor lies outside the viewport. */
public final class ZBuffer {
    private record Entry(Sprite sprite, int depth) {}
    private final ArrayList<Entry> sprites = new ArrayList<>();
    private boolean drawSpriteBounds;

    public synchronized void addSprite(Sprite sprite, int depth) {
        if (sprite == null || sprite.image == null) throw new IllegalArgumentException("Missing sprite image");
        sprites.add(new Entry(sprite, depth));
    }

    public synchronized void drawSpriteBounds(boolean on) { drawSpriteBounds = on; }

    public synchronized void draw(Graphics graphics, int x, int y, Dimension size) {
        sprites.sort(Comparator.comparingInt(Entry::depth));
        for (Entry entry : sprites) {
            Sprite sprite = entry.sprite();
            graphics.drawImage(sprite.image, sprite.x - x, sprite.y - y, null);
            if (drawSpriteBounds) {
                graphics.drawRect(sprite.x - x - 1, sprite.y - y - 1,
                        sprite.image.getWidth() + 1, sprite.image.getHeight() + 1);
            }
        }
        sprites.clear();
    }
}
