package client;

public class NukeAnimation {
    private NukeSpriteSet set;

    public NukeAnimation(String set) {
        this.set = NukeSpriteSet.load(set);
    }

    private int moveIndex = 0;

    private final long timePeriod = 75;
    private long begTime = 0;
    private long lastTimeStep = 0;

    public Sprite getSprite(long curTime) {
        Sprite tmpSpr;
        long tmpTime;

        tmpTime = (long) ((curTime - begTime) / timePeriod);

        if (tmpTime == 0) {
            lastTimeStep = tmpTime;
            moveIndex = 0;
            tmpSpr = set.getSprite(moveIndex);
            return tmpSpr;
        } else if (lastTimeStep != tmpTime) {
            if (moveIndex >= set.getSpriteCount() || set.getSpriteCount() == 1) {
                moveIndex = 0;
            }
            tmpSpr = set.getSprite(moveIndex++);
            lastTimeStep = tmpTime;
            return tmpSpr;
        } else {
            return set.getSprite(0);
        }
    }
}
