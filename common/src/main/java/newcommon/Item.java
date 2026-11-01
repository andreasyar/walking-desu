package newcommon;

/**
 * Общая часть всех предметов.
 * @author sorc
 */
public abstract class Item extends Entity {

    /**
     * Создаёт новый предмет.
     * @param id Идентификатор нового предмета.
     */
    protected Item(long id) {
        super(id);
    }
}
