package wand6.client;

import client.RedrawTask;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import wand6.client.messages.HelloMessage;
import wand6.client.messages.NameMessage;

public class Wand {
    public static void main(String[] args) {
        new Wand(args);
    }

    private Wand(String[] args) {
        String ip;
        int port;

        if (args.length == 2) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
        } else {
            ip = "localhost";
            port = 45000;
        }

        Executor executor = Executors.newCachedThreadPool();
        ServerInteraction inter = new ServerInteraction(ip, port);
        MessageManager.init(inter);
        inter.run();
        GUI gui = new GUI(null, inter);
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
