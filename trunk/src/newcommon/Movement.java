package newcommon;

import java.awt.Point;

/**
 * Общая часть любого движения.
 * @author sorc
 */
public abstract class Movement implements Movable {

    /**
     * Флаг движения, который показывает, происходит ли движение в данный момент
     * или нет.
     */
    protected boolean isMove;

    /**
     * Текущая точка траектории движения.
     */
    protected final Point cur = new Point();

    /**
     * Время начала движения.
     */
    protected long begTime;

    /**
     * Время окончания движения.
     */
    protected long endTime;

    /**
     * Скорость движения.
     */
    protected double speed;
}
