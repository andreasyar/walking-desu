package client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;

class NukeBoltMovement {
    public BufferedImage getSprite() {
        return null;
    }
    private static final String[] moveS = new String[] {"img/power_of_watering_pot_01.png"};
    private static ArrayList<BufferedImage> move = null;

    private int moveIndex = 0;

    private final long timePeriod = 75;
    private long begTime = 0;
    private long lastTimeStep = 0;
    private BufferedImage curMoveSpr = null;

    public NukeBoltMovement(long time) {
        begTime = time;
        if (move == null) {
            move = new ArrayList<BufferedImage>();
            ClassLoader cl = this.getClass().getClassLoader();
            URL url = null;

            move = new ArrayList<BufferedImage>();

            for (int i = 0; i < moveS.length; i++) {
                try {
                    url = cl.getResource(moveS[i]);
                    if (url != null) {
                        move.add(ImageIO.read(url));
                    } else {
                        move.add(ImageIO.read(new File(moveS[i])));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }

    public BufferedImage getSprite(long curTime) {
        long tmp;

        tmp = (long) ((curTime - begTime) / timePeriod);

        if (tmp == 0) {
            lastTimeStep = tmp;
            moveIndex = 0;
            curMoveSpr = move.get(moveIndex);
        } else if (lastTimeStep != tmp) {
            if (moveIndex >= move.size() || move.size() == 1) {
                moveIndex = 0;
            }
            curMoveSpr = move.get(moveIndex++);
            lastTimeStep = tmp;
        }

        return curMoveSpr;
    }
}
