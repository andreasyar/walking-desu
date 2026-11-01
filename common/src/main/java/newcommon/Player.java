package newcommon;

/**
 * Общая часть всех игроков.
 * @author sorc
 */
public abstract class Player extends Unit {

    /**
     * Движение.
     */

    /**
     * Создаёт нового игрока.
     * @param id Идентификатор нового игрока.
     */
    protected Player(long id) {
        super(id);
    }
}
