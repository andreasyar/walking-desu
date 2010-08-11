package client;

public abstract class NukeAnimation {
    protected DirectionalSpriteSet set;
    protected Direction direction;

    protected NukeAnimation(String set) {
        this.set = DirectionalSpriteSet.load(set);
    }
}
