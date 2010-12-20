package client;

//import java.awt.Point;
//
//public class MonsterDeathAnimation extends DeathAnimation {
//
//    private int period; // Количество спрайтов в наборе
//
//    private long step;  // Время показа каждого спрайта (или время смены спрайта)
//    private int delay;  // Задержка до очередного проигрывания
//    private long begTime = 0;
//
//    public MonsterDeathAnimation(String set) {
//        super(set);
//        if ("poring".equals(set)) {
//            this.step = 150;
//            this.delay = 0;
//        } else {
//            System.err.println("Death sprite set " + set + " not supported.");
//            System.exit(1);
//        }
//    }
//
//    @Override
//    public final Sprite getSprite(long curTime, Point curPos) {
//        long tmpTime;
//        Sprite tmpSpr;
//
//        if (step * period + delay == 0) {
//            System.err.println("Wow! " + (step * period + delay));
//            return set.getSprite(direction, period - 1);
//        }
//        tmpTime = (curTime - begTime) % (step * period + delay);
//
//        if (curTime > begTime + step * period + delay) {
//            done = true;
//            return set.getSprite(direction, period - 1);
//        }
//
//        if (tmpTime <= step * period) {
//            int sprIndex = (int) (((tmpTime % (step * period)) / step));
//            if (sprIndex < 0) {
//                System.err.println("Something wery bad happen right now.");
//            }
//            if (sprIndex < period && sprIndex >= 0) {
//                tmpSpr = set.getSprite(direction, sprIndex);
//            } else {
//                tmpSpr = set.getSprite(direction, 0);
//            }
//            tmpSpr.x = curPos.x - tmpSpr.image.getWidth() / 2;
//            tmpSpr.y = curPos.y - tmpSpr.image.getHeight();
//            tmpSpr.baseX = curPos.x;
//            tmpSpr.baseY = curPos.y;
//            return tmpSpr;
//        } else {
//
//            // Now time to delay before new animation cycle. So return last
//            // sprite from stand animation.
//            tmpSpr = set.getSprite(direction, period - 1);
//            tmpSpr.x = curPos.x - tmpSpr.image.getWidth() / 2;
//            tmpSpr.y = curPos.y - tmpSpr.image.getHeight();
//            tmpSpr.baseX = curPos.x;
//            tmpSpr.baseY = curPos.y;
//            return tmpSpr;
//        }
//    }
//
//    @Override
//    public void run(Direction direction, long begTime) {
//        done = false;
//        this.begTime = begTime;
//        this.direction = direction;
//        period = super.getSpriteCount();
//    }
//}
