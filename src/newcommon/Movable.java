package newcommon;

/**
 * Интерфейс перемещаемого объекта.
 * @author sorc
 */
public interface Movable {

    // <editor-fold defaultstate="expanded" desc="Position">
    /**
     * Возвращает X-координату начальной точки траектории движения.
     * @return X-координата начальной точки траектории движения.
     */
    public abstract int getBegX();

    /**
     * Возвращает Y-координату начальной точки траектории движения.
     * @return Y-координата начальной точки траектории движения.
     */
    public abstract int getBegY();

    /**
     * Возвращает X-координату конечной точки траектории движения.
     * @return X-координата конечной точки траектории движения.
     */
    public abstract int getEndX();

    /**
     * Возвращает Y-координату конечной точки траектории движения.
     * @return Y-координата конечной точки траектории движения.
     */
    public abstract int getEndY();

    /**
     * Возвращает X-координату текущей точки траектории движения.
     * @return X-координата текущей точки траектории движения.
     */
    public abstract int getCurX();

    /**
     * Возвращает Y-координату текущей точки траектории движения.
     * @return Y-координата текущей точки траектории движения.
     */
    public abstract int getCurY();
    // </editor-fold>

    // <editor-fold defaultstate="expanded" desc="Time">
    /**
     * Возвращает время начала движения.
     * @return Время начала движения.
     */
    public abstract long getBegTime();

    /**
     * Возвращает время окончания движения.
     * @return Время окончания движения.
     */
    public abstract long getEndTime();
    // </editor-fold>

    // <editor-fold defaultstate="expanded" desc="Speed">
    /**
     * Возвращает скорость движения.
     * @return скорость движения.
     */
    public abstract double getSpeed();

    /**
     * Устанавливает скорость движения.
     * @param speed Новая скорость движения.
     */
    public abstract void setSpeed(double speed);
    // </editor-fold>

    // <editor-fold defaultstate="expanded" desc="Control and status">
    /**
     * Возвращает флаг движения.
     */
    public abstract boolean isMove();

    /**
     * Останавливает движение.
     */
    public abstract void stop();
    // </editor-fold>
}
