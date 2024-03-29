package client;

public class MonsterDeathController implements Runnable {

    private final GameField field;

    public MonsterDeathController(GameField field) {
        this.field = field;
    }

    @Override
    public void run() {
        field.delDeadMonsters();
    }

}
