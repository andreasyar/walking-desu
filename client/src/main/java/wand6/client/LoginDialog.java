package wand6.client;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/** The new client owns its login UI; no GameField or 3.x classes are required. */
final class LoginDialog {
    static String chooseName(JFrame parent) {
        if (Boolean.getBoolean("walkingdesu.testMode")) {
            return requireValid(System.getProperty("walkingdesu.testNick"));
        }
        while (true) {
            String name = JOptionPane.showInputDialog(parent, "Nick (3–10 symbols):", "Desu");
            if (name == null) { return null; }
            try { return requireValid(name); }
            catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Choose nick", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String requireValid(String name) {
        if (name == null || name.length() < 3 || name.length() > 10
                || name.indexOf('"') >= 0 || name.indexOf('\n') >= 0 || name.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Nick must be 3–10 symbols, without quotes or line breaks.");
        }
        return name;
    }
}
