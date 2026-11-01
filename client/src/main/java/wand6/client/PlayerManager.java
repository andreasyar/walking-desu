package wand6.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import wand6.client.exceptions.PlayerManagerException;
import wand6.common.Player;

class PlayerManager {

    private static PlayerManager self = null;

    private volatile Player selfPlayer = null;
    private MovementAnimation walking;
    private StandAnimation standing;
    private Direction direction = Direction.SOUTH;
    Player selfPlayer() { return selfPlayer; }
    private final ArrayList<Player> players = new ArrayList<Player>();

    static PlayerManager getInstance() {
        if (self == null) {
            self = new PlayerManager();
        }

        return self;
    }

    private PlayerManager() {}

    long createPlayer(long id, String name, String spriteSetName) {
        Player player = new Player(id, name, spriteSetName);
        synchronized (players) {
            players.add(player);
        }
        return player.getId();
    }

    long createSelfPlayer(long id, String name, String spriteSetName) {
        selfPlayer = new Player(id, name, spriteSetName);
        walking = new MovementAnimation(spriteSetName);
        standing = new StandAnimation(spriteSetName);
        standing.run(direction, 0);
        synchronized (players) {
            players.add(selfPlayer);
        }
        return selfPlayer.getId();
    }

    long getSelfPlayerId() throws NullPointerException {
        return selfPlayer.getId();
    }

    void setSelfPlayerText(String string) throws NullPointerException {
        selfPlayer.setText(string);
    }

    int getSelfPlayerX() throws NullPointerException {
        return selfPlayer.getCurX();
    }

    int getSelfPlayerY() throws NullPointerException {
        return selfPlayer.getCurY();
    }

    void drawPlayers(Graphics g, Camera camera, long time) {
        if (selfPlayer == null) return;
        var movement = selfPlayer.getMovement();
        var position = movement.position(time);
        var point = new java.awt.Point(position.x(), position.y());
        Sprite sprite = movement.isMovingAt(time)
                ? walking.getSprite(point) : standing.getSprite(time, point);
        g.drawImage(sprite.image, sprite.x - camera.x(), sprite.y - camera.y(), null);
        g.setColor(Color.WHITE);
        String name = selfPlayer.getName();
        g.drawString(name, point.x - camera.x() - g.getFontMetrics().stringWidth(name) / 2,
                sprite.y - camera.y() - 4);
    }

    String getSelfPlayerText() throws NullPointerException {
        return selfPlayer.getText();
    }

    void movePlayer(long playerId, int bx, int by, int x, int y, long begTime) throws PlayerManagerException {
        synchronized(players) {
            for (Player player : players) {
                if (player.getId() == playerId) {
                    player.getMovement().start(bx, by, x, y, begTime);
                    if (bx != x || by != y) {
                        walking.run(bx, by, x, y, 10.0);
                        direction = walking.getDirection();
                    }
                    standing.run(direction, player.getMovement().getEndTime());
                    return;
                }
            }

            throw new PlayerManagerException("Player id=" + playerId + " not found.");
        }
    }
}
