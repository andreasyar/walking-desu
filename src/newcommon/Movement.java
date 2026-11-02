package newcommon;

public interface Movement {

    public abstract int getBegX();

    public abstract int getBegY();

    public abstract int getEndX();

    public abstract int getEndY();

    public abstract int getCurX();

    public abstract int getCurY();

    public abstract long getBegTime();

    public abstract long getEndTime();

    public abstract double getSpeed();

    public abstract void setSpeed(double speed);

    public abstract boolean isMove();

    public abstract void stop();
}
