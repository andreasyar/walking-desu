package server.javatestserver;

import newcommon.Monster;
import newcommon.Player;
import newcommon.Unit;
import newcommon.exceptions.UnitException;
import newcommon.items.Etc;

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

    void sendUseSkillMessage(Unit unit, long skillId, long begTime) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void sendHitMessage(Player attacker, Player target, int i) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void sendMonsterDeadMessage(Player attacker, Monster target) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void sendRemoveEtcFromInventory(Player target, Etc gold) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void sendPlayerDeadMessage(Player target) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void sendTeleportMessage(Player target, int i, int i0) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
