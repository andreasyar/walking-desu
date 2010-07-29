package client;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

public class GeoDataController implements Runnable {

    private ScheduledFuture future = null;
    private boolean canceled = false;
    private final ArrayList<WanderingPolygon> geoData;
    private final ArrayList<Player> players;

    public GeoDataController(ArrayList<WanderingPolygon> geoData, ArrayList<Player> players) {
        this.geoData = geoData;
        this.players = players;
    }

    public void run() {
        if (!canceled) {
            synchronized (players) {
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
