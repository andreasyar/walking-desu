package wand6.client;

import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;
import wand6.client.messages.HelloMessage;
import wand6.client.messages.NameMessage;

public class Wand {
    public static void main(String[] args) {
        ServerInteraction inter = null;
        try {
            if (args.length != 0 && args.length != 2) {
                throw new IllegalArgumentException("Usage: wand6-client [host port]");
            }
            // Validate assets before opening sockets or windows.
            ClientAssets.summer();
            ClientAssets.image("img/coin_stacks_gold.png");
            DirectionalSpriteSet.load("peasant_walk");
            DirectionalSpriteSet.load("peasant_stand");
            for (String state : new String[]{"attack", "death", "bolt"}) DirectionalSpriteSet.load("peasant_" + state);
            if (GraphicsEnvironment.isHeadless()) {
                throw new IllegalStateException("wand6-client needs a graphical desktop (DISPLAY/Wayland).");
            }
            String host = args.length == 2 ? args[0] : "localhost";
            int port = args.length == 2 ? Integer.parseInt(args[1]) : 45000;
            GUI[] gui = new GUI[1];
            String[] name = new String[1];
            // The modal login and every Swing operation run on the EDT.
            SwingUtilities.invokeAndWait(() -> name[0] = LoginDialog.chooseName(null));
            if (name[0] == null) { return; }
            inter = new ServerInteraction(host, port);
            MessageManager.init(inter);
            ServerInteraction connected = inter;
            SwingUtilities.invokeAndWait(() -> {
                gui[0] = new GUI(connected);
                gui[0].frame.setVisible(true);
                gui[0].redraw.start();
            });
            inter.run();
            inter.sendMessage(new HelloMessage());
            inter.sendMessage(new NameMessage(name[0]));
        } catch (Exception ex) {
            if (inter != null) { inter.close(); }
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            System.err.println("Cannot start wand6: " + cause.getMessage());
            System.exit(1);
        }
    }
}
