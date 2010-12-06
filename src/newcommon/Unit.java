package newcommon;

/**
 * Общая часть всех существ.
 * @author sorc
 */
public abstract class Unit extends Entity {

    /**
     * Максимальный уровень здоровья существа.
     */
    private int maxHealth;

    /**
     * Уровень здоровья существа.
     */
    private int health;

    /**
     * Создаёт новое существо.
     * @param id Идентификатор нового существа.
     */
    protected Unit(long id) {
        super(id);
    }

    // <editor-fold defaultstate="expanded" desc="Health">
    /**
     * Возвращает максимальный уровень здоровья существа.
     * @return Максимальный уровень здоровья существа.
     */
    protected abstract int getMaxHealth();

    /**
     * Устанавливает максимальный уровнь здоровья существа.
     * @param maxHealth Максимальный уровнь здоровья существа.
     */
    protected abstract void setMaxHealth(int maxHealth);

    /**
     * Возвращает уровень здоровья существа.
     * @return Уровень здоровья существа.
     */
    protected abstract int getHealth();

    /**
     * Возвращает уровень здоровья существа.
     * @param health Уровнь здоровья существа.
     */
    protected abstract void setHealth(int health);
    // </editor-fold>
}
