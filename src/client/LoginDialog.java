package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class LoginDialog extends JDialog implements ActionListener, PropertyChangeListener, KeyListener {
    private String typedText = "Desu";
    private JTextField textField;

    private JOptionPane optionPane;

    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";

    private DefaultNameTask task = null;

    public String getValidatedText() {
        return typedText;
    }

    public LoginDialog(JFrame frame) {
        super(frame, true);

        setTitle("Choose nick");

        textField = new JTextField(10);
        textField.setText("Desu");

        String msgString1 = "Select your nick.";
        String msgString2 = "(It must be 3-10 symbols long.)";
        Object[] array = {msgString1, msgString2, textField};

        Object[] options = {btnString1, btnString2};

        optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION, null, options, options[0]);

        setContentPane(optionPane);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });

        textField.addActionListener(this);
        textField.addKeyListener(this);

        optionPane.addPropertyChangeListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(btnString1);
        if (task != null) {
            task.cancel();
            task = null;
            setTitle("Choose nick");
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible()
                && (e.getSource() == optionPane)
                && (JOptionPane.VALUE_PROPERTY.equals(prop)
                || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                return;
            }

            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (btnString1.equals(value)) {
                typedText = textField.getText();
                if (typedText.length() >= 3 && typedText.length() <= 10) {
                    clearAndHide();
                } else {
                    textField.selectAll();
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Nick must be 3-10 symbols long.",
                            "Try again",
                            JOptionPane.ERROR_MESSAGE);
                    typedText = "Desu";
                    textField.requestFocusInWindow();
                    if (task != null) {
                        task.cancel();
                        task = null;
                        setTitle("Choose nick");
                    }
                }
            } else {
                if (task != null) {
                    task.cancel();
                    task = null;
                }
                typedText = "Desu";
                clearAndHide();
            }
        }
    }

    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {
        if (task != null) {
            task.cancel();
            task = null;
            setTitle("Choose nick");
        }
    }

    public void clearAndHide() {
        textField.setText(null);
        setVisible(false);
    }

    public void startTimer() {
        task = new DefaultNameTask();
    }

    private class DefaultNameTask extends TimerTask {

        public Timer timer; // TODO Where gentle stop timer?
        private int repeat = 20;
        private int step = 1000; // 1 sec

        public DefaultNameTask() {
            timer = new Timer();
            timer.schedule(this, 0, step);
        }

        public void run(){
            if(--repeat < 0) {
                this.cancel();
                clearAndHide();
                return;
            }
            setTitle("Choose nick. " + repeat + " ...");
        }
    }
}