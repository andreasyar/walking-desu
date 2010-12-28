package wand6.client;

import java.awt.Color;
import java.awt.Graphics;
import wand6.common.MapFragment;

class CMapFragment extends MapFragment {

    private boolean loaded;

    boolean isLoaded() {
        return loaded;
    }

    void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public void draw(Graphics g, int x, int y) {
        Color savedColor = g.getColor();
        g.setColor(Color.BLACK);
        g.drawRect(x + 1,
                   y + 1,
                   CMapFragment.width - 1,
                   CMapFragment.height - 1);
        g.setColor(savedColor);
    }
}
