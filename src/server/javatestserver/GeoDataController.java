package server.javatestserver;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;

public class GeoDataController implements Runnable {

    private ScheduledFuture future = null;
    private boolean canceled = false;
    private final ArrayList<WanderingPolygon> geoData;
    private final LinkedBlockingQueue<Player> players;

    public GeoDataController(LinkedBlockingQueue<Player> players) {
        this.geoData = WanderingMap.getGeoData();
        this.players = players;
    }

    @Override
    public void run() {
        if (!canceled) {
            for (WanderingPolygon poly : geoData) {
                if (poly.getType() == WanderingPolygon.WallType.SPECIAL) {
                    for (Player player : players) {
                        if (poly.contains(player.getCurPos())) {
                            poly.trigger(player);
                        }
                    }
                }
            }
        }
    }

    public void setScheduledFuture(ScheduledFuture future) {
        this.future = future;
    }

    public void cancel() {
        if (future != null) {
            future.cancel(true);
            canceled = true;
        }
    }
}
