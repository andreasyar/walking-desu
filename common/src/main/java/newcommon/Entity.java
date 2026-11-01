package newcommon;

/**
 * Общая часть любой сущности в мире.
 * @author sorc
 */
public abstract class Entity {

    /**
     * Уникальный идентификатор сущности.
     */
    private final long id;

    /**
     * Имя сущности.
     */
    private String name;

    /**
     * Создаёт новую сущность.
     * @param id Идентификатор новой сущности.
     */
    protected Entity(long id) {
        this.id = id;
    }

    // <editor-fold defaultstate="expanded" desc="Name">
    /**
     * Возвращает имя сущности.
     * @return имя сущности.
     */
    protected String getName() {
        return name;
    }

    /**
     * Даёт имя сущности.
     * @param name имя.
     */
    protected void setName(String name) {
        this.name = name;
    }
    // </editor-fold>
}
