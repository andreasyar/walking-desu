/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package common;

import java.io.Serializable;

public class HMapMessage implements Message, Serializable {

    private final MessageType type = MessageType.HMAP;
    private final int[][] hmap;
    private final int idx;
    private final int idy;

    public HMapMessage(int[][] hmap, int idx, int idy) {
        this.hmap = hmap;
        this.idx = idx;
        this.idy = idy;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public int[][] getHmap() {
        return hmap;
    }

    public int getIdx() {
        return idx;
    }

    public int getIdy() {
        return idy;
    }

    @Override
    public String toString() {
        return "(" + idx + ", " + idy + ")" + " map fragment.";
    }
}
