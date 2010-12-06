package client;

import common.GoldCoinItem;

/**
 * Gold coin(s) for client.
 */
public class WGoldCoinItem extends GoldCoinItem implements WItem, WDrawable {

    private LayGroundAnimation animation;

    public WGoldCoinItem(long id, int count) {
        super(id, count);
        animation = new LayGroundAnimation("coin_stacks_gold");
    }

    public WGoldCoinItem(long id) {
        super(id);
    }

    @Override
    public Sprite getSprite() {
        return animation.getSprite(x, y);
    }
}
