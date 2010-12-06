/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

import java.awt.Dimension;
import java.awt.Graphics;

/**
 *
 * @author sorc
 */
interface WDrawable {

    //public abstract Sprite getSprite(Graphics g, int x, int y);

    public abstract void draw(Graphics g, int x, int y, Dimension d);

}
