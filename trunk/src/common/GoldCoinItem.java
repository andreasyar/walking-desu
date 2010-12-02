package common;

/**
 * Gold coin(s).
 */
public class GoldCoinItem extends MultiItem {

    public GoldCoinItem(long id) {
        super(id, "Gold");
    }

    public GoldCoinItem(long id, int count) {
        super(id, "Gold");
        setCount(count);
    }
}
