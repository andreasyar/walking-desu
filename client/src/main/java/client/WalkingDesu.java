package client;

import java.awt.AWTError;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;

public class WalkingDesu {
    public static void main(String[] args) {
        try {
            // Check before GameField starts threads or ServerInteraction opens a socket.
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            new WalkingDesu(args);
        } catch (HeadlessException | AWTError | UnsatisfiedLinkError ex) {
            System.err.println("Cannot start the graphical client: " + ex);
            System.err.println("Use a Java runtime with GUI support (not headless) "
                    + "and run from a terminal in your desktop session.");
            System.err.println("On Fedora, install java-<version>-openjdk "
                    + "for the selected Java version. See README.md.");
            System.err.println("java.home=" + System.getProperty("java.home")
                    + ", DISPLAY=" + System.getenv("DISPLAY")
                    + ", WAYLAND_DISPLAY=" + System.getenv("WAYLAND_DISPLAY"));
            System.exit(1);
        }
    }

    private WalkingDesu(String[] args) {
        GameField field = new GameField();
        field.startSelfExecution();
        ClientMapFragment.setWidth(1024);
        ClientMapFragment.setHeight(1024);
        ClientMapFragment.setCellW(32);
        ClientMapFragment.setCellH(32);

        field.addMapFragment(new ClientMapFragment(0, 0, new int[][]{}));

        /*WGoldCoinItem g = new WGoldCoinItem(0L, 9);
        g.setOnGround(true);
        g.setX(100);
        g.setY(100);
        field.asyncAddItem(g);
        g = new WGoldCoinItem(0L, 18);
        g.setOnGround(true);
        g.setX(200);
        g.setY(100);
        field.asyncAddItem(g);*/

        Executor executor = Executors.newCachedThreadPool();
        ServerInteraction inter = new ServerInteraction(field, executor, args[0], Integer.parseInt(args[1]));

        WanderingGUI gui = new WanderingGUI(field, inter);
        try {
            SwingUtilities.invokeAndWait(gui);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        try {
            inter.run(gui.dialog.getValidatedText());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        gui.showPanel();
        executor.execute(new RedrawTask(gui.panel));
    }
}
