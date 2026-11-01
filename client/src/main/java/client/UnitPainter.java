package client;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.awt.font.TextAttribute;

/** Paint-time presentation. No Graphics, font metrics or screen device is retained. */
final class UnitPainter {
    private UnitPainter() {}

    static Sprite sprite(Drawable drawable, Graphics context) {
        if (!(drawable instanceof WUnit unit)) return drawable.getSprite();
        synchronized (unit) {
            Sprite source = unit.getSprite();
            if (source == null) return null;
            FontMetrics metrics = context.getFontMetrics();
            String name = unit.getNick();
            int width = Math.max(source.image.getWidth(), metrics.stringWidth(name) + 2);
            int labelHeight = metrics.getHeight() + 1;
            BufferedImage image = new BufferedImage(width, source.image.getHeight() + labelHeight,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.setFont(context.getFont());
                graphics.drawImage(source.image, (width - source.image.getWidth()) / 2, labelHeight, null);
                graphics.setColor(Color.BLACK);
                graphics.drawString(name, (width - metrics.stringWidth(name)) / 2, metrics.getAscent());
            } finally {
                graphics.dispose();
            }
            Sprite result = new Sprite();
            result.image = image;
            result.x = source.x - (width - source.image.getWidth()) / 2;
            result.y = source.y - labelHeight;
            result.baseX = source.baseX;
            result.baseY = source.baseY;
            return result;
        }
    }

    static void drawText(WUnit unit, Graphics context, int cameraX, int cameraY) {
        synchronized (unit) {
            String text = unit.getText();
            if (text == null || text.isEmpty()) return;
            Sprite sprite = unit.getSprite();
            if (sprite == null) return;
            Graphics2D graphics = (Graphics2D) context.create();
            try {
                int x = sprite.x - cameraX + sprite.image.getWidth();
                int y = sprite.y - cameraY;
                graphics.setColor(new Color(.1f, 1f, .3f, .7f));
                graphics.fillRoundRect(x, y, 150, 100, 10, 10);
                graphics.clipRect(x, y, 150, 100);
                graphics.setColor(Color.BLACK);
                AttributedString paragraph = new AttributedString(text);
                paragraph.addAttribute(TextAttribute.FONT, context.getFont());
                LineBreakMeasurer lines = new LineBreakMeasurer(paragraph.getIterator(), graphics.getFontRenderContext());
                float baseline = y;
                while (lines.getPosition() < text.length() && baseline < y + 100) {
                    TextLayout line = lines.nextLayout(146);
                    baseline += line.getAscent();
                    line.draw(graphics, x + 2, baseline);
                    baseline += line.getDescent() + line.getLeading();
                }
            } finally {
                graphics.dispose();
            }
        }
    }
}
