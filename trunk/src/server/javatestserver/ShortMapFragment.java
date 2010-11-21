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
public class ShortMapFragment implements Serializable {
    public final int[][] hmap;
    public final int idx;
    public final int idy;

    public ShortMapFragment(int[][] hmap, int idx, int idy) {
        this.hmap = hmap;
        this.idx = idx;
        this.idy = idy;
    }
}
