package server.javatestserver;

import newcommon.CurveMovement;
import newcommon.Unit;

class NukeBolt {

    private long attackerId;
    private long targetId;
    private CurveMovement mv = new CurveMovement();

    public NukeBolt(Unit attacker, Unit target, long begTime, double speed) {
        attackerId = attacker.getId();
        targetId = target.getId();

        mv.start(attacker.getCurX(),
                 attacker.getCurY(),
                 target.getCurX(),
                 target.getCurY(),
                 begTime,
                 speed);
    }

    public boolean isFlight() {
        return mv.isMove();
    }

    public long getAttackerId() {
        return attackerId;
    }

    public long getTargetId() {
        return targetId;
    }
}
