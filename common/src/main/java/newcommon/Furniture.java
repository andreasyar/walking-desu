package newcommon;

/**
 * Общая часть всех элементов обстановки.
 * @author sorc
 */
public abstract class Furniture extends Entity {

    /**
     * Создаёт новый элемент обстановки.
     * @param id Идентификатор нового элемента обстановки.
     */
    protected Furniture(long id) {
        super(id);
    }
}
