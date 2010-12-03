package newcommon;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Движение по кривой, заданной точками.
 * @author sorc
 */
public class CurveMovement extends Movement {

    /**
     * Точки ломаной движения.
     */
    private final ArrayList<TrackPoint> track = new ArrayList<TrackPoint>();

    // <editor-fold defaultstate="collapsed" desc="Position">
    /**
     * Возвращает X-координату начальной точки траектории движения.
     * @return X-координата начальной точки траектории движения.
     */
    @Override
    public int getBegX() {
        return track.get(0).getPoint().x;
    }

    /**
     * Возвращает Y-координату начальной точки траектории движения.
     * @return Y-координата начальной точки траектории движения.
     */
    @Override
    public int getBegY() {
        return track.get(0).getPoint().y;
    }

    /**
     * Возвращает X-координату конечной точки траектории движения.
     * @return X-координата конечной точки траектории движения.
     */
    @Override
    public int getEndX() {
        return track.get(track.size() - 1).getPoint().x;
    }

    /**
     * Возвращает Y-координату конечной точки траектории движения.
     * @return Y-координата конечной точки траектории движения.
     */
    @Override
    public int getEndY() {
        return track.get(track.size() - 1).getPoint().y;
    }

    /**
     * Возвращает X-координату текущей точки траектории движения.
     * @return X-координата текущей точки траектории движения.
     */
    @Override
    public int getCurX() {
        updateCurPos();
        return cur.x;
    }

    /**
     * Возвращает Y-координату текущей точки траектории движения.
     * @return Y-координата текущей точки траектории движения.
     */
    @Override
    public int getCurY() {
        updateCurPos();
        return cur.y;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Time">
    /**
     * Возвращает время начала движения.
     * @return Время начала движения.
     */
    @Override
    public long getBegTime() {
        return begTime;
    }

    /**
     * Возвращает время окончания движения.
     * @return Время окончания движения.
     */
    @Override
    public long getEndTime() {
        if (isMove) {
            endTime = 0L;

            synchronized (track) {
                for (int i = 1; i < track.size(); i++) {
                    endTime += (long) (track.get(i - 1).getPoint().distance(track.get(i).getPoint()) / speed);
                }
            }
        }

        return endTime;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Speed">
    /**
     * Возвращает скорость движения.
     * @return скорость движения.
     */
    @Override
    public double getSpeed() {
        return speed;
    }

    /**
     * Устанавливает скорость движения.
     * @param speed Новая скорость движения.
     */
    @Override
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    // </editor-fold>

    /**
     * Возвращает флаг движения.
     */
    @Override
    public boolean isMove() {
        updateCurPos();
        return isMove;
    }

    /**
     * Начинает движение по прямой.
     * @param begX X-координата начальной точки движения.
     * @param begY Y-координата начальной точки движения.
     * @param endX X-координата конечной точки движения.
     * @param endY Y-координата конечной точки движения.
     * @param begTime Время начала движения.
     * @param speed Скорость движения. Должна быть больше нуля.
     */
    public void start(int begX,
                      int begY,
                      int endX,
                      int endY,
                      long begTime,
                      double speed) throws IllegalArgumentException {

        if (speed < 0.0) {
            throw new IllegalArgumentException("Movement speed must be >= 0.0");
        }

        track.clear();
        track.add(new TrackPoint(new Point(begX, begY), true));
        track.add(new TrackPoint(new Point(endX, endY), false));
        cur.setLocation(track.get(0).getPoint());
        this.begTime = begTime;
        this.speed = speed;
        isMove = true;
        getEndTime();   // Just for calculate new end time.
    }

    /**
     * Начинает движение по трактории.
     * @param track Траектория. Должна содержать как минимум две точки.
     * @param begTime Время начала движения.
     * @param speed Скорость движения. Должна быть больше нуля.
     */
    public void start(ArrayList<Point> track,
                      long begTime,
                      double speed) throws IllegalArgumentException {

        if (speed < 0.0) {
            throw new IllegalArgumentException("Movement speed must be >= 0.0");
        }
        if (track == null) {
            throw new IllegalArgumentException("Track is null!");
        }
        if (track.size() < 2) {
            throw new IllegalArgumentException("Track must contain at least 2 points!");
        }

        this.track.clear();
        this.track.add(new TrackPoint(track.get(0), true));

        for (int i = 1; i < track.size(); i++) {
            this.track.add(new TrackPoint(track.get(i), false));
        }

        cur.setLocation(this.track.get(0).getPoint());
        this.begTime = begTime;
        this.speed = speed;
        isMove = true;
        getEndTime();   // Just for calculate new end time.
    }

    /**
     * Останавливает движение.
     */
    @Override
    public void stop() {
        updateCurPos();
        endTime = ServerTime.getInstance().getTimeSinceStart();
        isMove = false;
    }

    /**
     * Обновляет текущую точку траектории движения.
     */
    private void updateCurPos() {
        if (isMove) {

            // Начальная точка участка ломаной, по которому мы сейчас движемся.
            Point beg = null;

            // Конечная точка участка ломаной, по которому мы сейчас движемся.
            Point end = null;

            // Номер участка ломаной, по которому мы сейчас движемся.
            int i = 1;

            // Найдем первую точку ломаной, которой мы ещё не достигли.
            // Таковая обязательно должна быть, так как движение по кривой ещё
            // не завершено.
            synchronized (track) {

                // Начинаем проход по ломаной со второй точки, так как самая
                // первая всегда достигнута, потому что она начальная точка
                // всего движения.
                for (; i < track.size(); i++) {
                    if (!track.get(i).isRiched()) {
                        beg = track.get(i - 1).getPoint();
                        end = track.get(i).getPoint();
                        break;
                    }
                }
            }

            long curTime = ServerTime.getInstance().getTimeSinceStart();
            double sqrt = Math.sqrt(Math.pow(Math.abs(end.x - beg.x), 2) + Math.pow(Math.abs(end.y - beg.y), 2));

            cur.x = (int) (beg.x + ((end.x - beg.x) / sqrt) * speed * (curTime - begTime));
            cur.y = (int) (beg.y + ((end.y - beg.y) / sqrt) * speed * (curTime - begTime));

            // Прошли этот участок ломаной.
            if (beg.x > end.x && end.x > cur.x
                    || beg.x < end.x && end.x < cur.x
                    || beg.y > end.y && end.y > cur.y
                    || beg.y < end.y && end.y < cur.y) {

                track.get(i).setRiched(true);
                cur.setLocation(end.x, end.y);

                // Если прошли последний участок ломаной, то движение завершено.
                if (i == track.size() - 1) {
                    isMove = false;
                }
            }
        }
    }

    /**
     * Точка ломаной движения.
     */
    private class TrackPoint {

        /**
         * Точка ломаной движения.
         */
        private Point point;

        /**
         * Флаг. Была ли достигнута точка в ходе движения.
         */
        private boolean riched;

        /**
         * Создаёт новую точку ломаной движения.
         * @param point Точка.
         * @param riched Была ли достигнута точка в ходе движения.
         */
        public TrackPoint(Point point, boolean riched) {
            this.point = point;
            this.riched = riched;
        }

        /**
         * Возвращает точку ломаной движения.
         * @return Точка ломаной движения.
         */
        public Point getPoint() {
            return point;
        }

        /**
         * Возвращает значение флага. Была ли достигнута точка в ходе движения.
         * @return Была ли достигнута точка в ходе движения.
         */
        public boolean isRiched() {
            return riched;
        }

        /**
         * Устанавливает значение флага. Была ли достигнута точка в ходе движения.
         * @param riched Была ли достигнута точка в ходе движения.
         */
        public void setRiched(boolean riched) {
            this.riched = riched;
        }

    }
}
