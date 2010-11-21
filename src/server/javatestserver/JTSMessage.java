/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server.javatestserver;

import java.io.Serializable;

/**
 *
 * @author Sorc
 */
public class JTSMessage implements Serializable {
    private final Object data;
    private final JTSMessageTypes type;

    public JTSMessageTypes getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public JTSMessage(JTSMessageTypes type, Object data) {
        this.data = data;
        this.type = type;
    }

    @Override
    public String toString() {
        if (type == JTSMessageTypes.OTHER) {
            return (String) data;
        } else if (type == JTSMessageTypes.HMAP) {
            return "hmap";
        } else {
            return "error";
        }
    }
}
