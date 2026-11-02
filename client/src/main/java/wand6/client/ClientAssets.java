package wand6.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

/** Images are supplied separately in the distribution's assets directory. */
final class ClientAssets {
    private static BufferedImage summer;

    static BufferedImage image(String path) {
        URL resource = ClientAssets.class.getClassLoader().getResource(path);
        if (resource == null) throw new IllegalStateException("Missing wand6 asset " + path
                + ". Supply external assets; see CLIENT-ASSETS.md.");
        try {
            BufferedImage image = ImageIO.read(resource);
            if (image == null) throw new IOException("Unsupported image format");
            return image;
        } catch (IOException ex) { throw new IllegalStateException("Cannot read " + path, ex); }
    }

    static synchronized BufferedImage summer() {
        if (summer == null) {
            URL resource = ClientAssets.class.getClassLoader().getResource("img/summer.png");
            if (resource == null) {
                throw new IllegalStateException("Missing wand6 asset img/summer.png. "
                        + "Copy your atlas to <client distribution>/assets/img/summer.png. See CLIENT-ASSETS.md.");
            }
            try {
                summer = ImageIO.read(resource);
                if (summer == null || summer.getWidth() < 512 || summer.getHeight() < 672) {
                    throw new IllegalStateException("Invalid img/summer.png: a readable atlas of at least 512x672 is required.");
                }
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot read img/summer.png: " + ex.getMessage(), ex);
            }
        }
        return summer;
    }
}
