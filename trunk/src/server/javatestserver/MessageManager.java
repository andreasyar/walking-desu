package server.javatestserver;

import newcommon.Player;
import newcommon.exceptions.UnitException;

class MessageManager {

    private static MessageManager self = null;
    private JavaTestServer jts = JavaTestServer.getInstance();

    static MessageManager getInstance() {
        if (self == null) {
            self = new MessageManager();
        }

        return self;
    }

    private MessageManager() {}

    void sendAppearPlayerMessage(Player p1, Player p2) throws IllegalArgumentException {
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("Player p1 and player p2 cannot be null.");
        }
    }

    void sendDisappearPlayerMessage(Player p1, Player p2) throws IllegalArgumentException {
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("Player p1 and player p2 cannot be null.");
        }
    }

    void sendPlayerStateMessage(Player p1, Player p2) throws IllegalArgumentException, UnitException {
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("Player p1 and player p2 cannot be null.");
        }

        if (!p2.getText().isEmpty()) {
            
        }

        if (p2.isMove()) {
            
        } else if (p2.isDead()) {
            
        } else if (p2.isStand()) {
            
        } else if (p2.isUseSkill()) {
            
        } else {
            throw new UnitException("Illegal unit state.");
        }
    }

    void sendDeathDelayOver(Player corpse) throws IllegalArgumentException {
        if (corpse == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }
    }
}
