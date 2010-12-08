/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package common;

/**
 * Inventory.
 * @author sorc
 */
public class Inventory {

    /**
     * Gold coin(s).
     */
    private GoldCoin goldCoin = null;

    /**
     * Adds new gold coin to inventory if there is no gold coin yet or modify
     * count of gold coins if some gold coins already in inventory.
     * @param id id of new gold coin item.
     * @param count count of gold coins.
     */
    public void addGoldCoin(long id, int count) {
        if (goldCoin == null) {
            goldCoin = new GoldCoin(id, count);
        } else {
            goldCoin.setCount(goldCoin.getCount() + count);
        }
    }

    /**
     * Delete <i>count</i> of gold coin from inventory.
     * @param id id of new gold coin item.
     * @param count count of gold coins.
     */
    public void delGoldCoin(long id, int count) {
        if (goldCoin == null) {
            // TODO It is abnormal. We must react to this.
            System.err.println("We try to delete gold coins but we have no gold coins.");
        } else {
            goldCoin.setCount(goldCoin.getCount() - count);
            if (goldCoin.getCount() == 0) {
                goldCoin = null;
            } else if (goldCoin.getCount() < 0) {
                // TODO It is abnormal. We must react to this.
                goldCoin = null;
                System.err.println("We delete gold coins more than we have.");
            }
        }
    }

    /**
     * Return gold coins count. If we have no gold coins it return 0 anyway.
     * @return gold coins count.
     */
    public int getGoldCoinCount() {
        if (goldCoin == null) {
            return 0;
        } else {
            return goldCoin.getCount();
        }
    }
}
