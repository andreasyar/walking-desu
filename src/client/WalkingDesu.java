package client;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import server.javaserver.HelloMessage;
import server.javaserver.NameMessage;

public class WalkingDesu {
    public static void main(String[] args) {
        new WalkingDesu(args);
    }

    private WalkingDesu(String[] args) {
        GameField field = new GameField();
        field.startSelfExecution();
        ClientMapFragment.setWidth(1024);
        ClientMapFragment.setHeight(1024);
        ClientMapFragment.setCellW(32);
        ClientMapFragment.setCellH(32);

        field.addMapFragment(new ClientMapFragment(0, 0, new int[][]{}));

        Executor executor = Executors.newCachedThreadPool();
        ServerInteraction inter = new ServerInteraction(args[0], Integer.parseInt(args[1]));
        inter.run();
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
            inter.sendMessage(new HelloMessage());
            inter.sendMessage(new NameMessage(gui.dialog.getValidatedText()));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        gui.showPanel();
        executor.execute(new RedrawTask(gui.panel));
    }
}
