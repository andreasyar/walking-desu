package server.javatestserver;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import newcommon.Monster;
import newcommon.Player;
import newcommon.exceptions.InventoryException;
import newcommon.items.Etc;
import newcommon.items.Items;

class NukeAction implements Runnable {

    private NukeBolt bolt;
    private ScheduledFuture future = null;
    private boolean canceled = false;

    NukeAction(ScheduledFuture future, NukeBolt bolt) {
        this.bolt = bolt;
        this.future = future;
    }

    @Override
    public void run() {
        if (!canceled) {
            if (!bolt.isFlight()) {
                pvp(GameField.getInstance().getPlayer(bolt.getAttackerId()), GameField.getInstance().getPlayer(bolt.getTargetId()));
                pvm(GameField.getInstance().getPlayer(bolt.getAttackerId()), GameField.getInstance().getMonster(bolt.getTargetId()));
                mvm(GameField.getInstance().getMonster(bolt.getAttackerId()), GameField.getInstance().getMonster(bolt.getTargetId()));
                this.cancel();
            }
        }
    }

    void cancel() {
        if (future != null) {
            future.cancel(true);
            canceled = true;
        }
    }

    private void pvp(Player attacker, Player target) {
        if (attacker != null && target != null) {
            target.hit(50);
            MessageManager.getInstance().sendHitMessage(attacker, target, 50);
            if (target.isDead()) {
                MessageManager.getInstance().sendPlayerDeadMessage(target);
                ArrayList<Etc> tmpEtcs = target.getEtc(Items.GOLD);
                if (!tmpEtcs.isEmpty()) {
                    Etc gold = tmpEtcs.get(0);
                    try {
                        target.removeEtc(gold);
                    } catch (InventoryException ex) {
                        Logger.getLogger(NukeAction.class.getName()).log(Level.SEVERE, null, ex);
                        System.exit(1);
                    }
                    MessageManager.getInstance().sendRemoveEtcFromInventory(target, gold);
                    gold.setX(target.getCurX());
                    gold.setY(target.getCurY());
                    GameField.getInstance().asyncAddEtc(gold);
                }
                target.resurect();
                target.teleportTo(0, 0);
                MessageManager.getInstance().sendTeleportMessage(target, 0, 0);
            }
        }
    }

    private void pvm(Player attacker, Monster target) {
        if (attacker != null && target != null) {
            target.hit(50);
            if (target.isDead()) {
                GameField.getInstance().asyncRemoveMonster(target);
                MessageManager.getInstance().sendMonsterDeadMessage(attacker, target);
            }
        }
    }

    private void mvm(Monster attacker, Monster target) {
        if (attacker != null && target != null) {
            target.hit(50);
            if (target.isDead()) {
                GameField.getInstance().asyncRemoveMonster(target);
            }
        }
    }
}
