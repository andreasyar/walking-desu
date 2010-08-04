package client;

public abstract class DeathAnimation {

    private DirectionalSpriteSet set;
    protected Direction direction;

    protected DeathAnimation(String set) {
        this.set = DirectionalSpriteSet.load(set);
    }

    protected Sprite getSprite(int index) {
        return set.getSprite(direction, index);
    }

    protected int getSpriteCount() {
        return set.getSpriteCount(direction);
    }
}
