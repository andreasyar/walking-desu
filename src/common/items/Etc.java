package common.items;

import common.Inventory;

/**
 * Прочий предмет. Например: золото, кусок металла, пробка. Все предметы, для
 * которых нет (пока?) обобщения.
 * @author CatsPaw
 */
public abstract class Etc extends Item {

    /**
     * Тип прочего предмета.
     */
    private final Items type;

    /**
     * Создаёт новый прочий предмет.
     * @param id id нового предмета.
     * @param name имя нового предмета.
     * @param type тип нового предмета.
     */
    protected Etc(long id, String name, Items type) {
        super(id, name);
        this.type = type;
    }

    /**
     * Возвращает тип прочего предмета.
     * @param тип прочего предмета.
     */
    public Items getType() {
        return type;
    }

    /**
     * Adds etc item to inventory <i>inv</i>.
     * @param inv inventory.
     */
    public void addToInventory(Inventory inv) {
        inv.addEtc(this);
    }

    /**
     * Removes etc item from inventory <i>inv</i>.
     * @param inv inventory.
     */
    public void removeFromInventory(Inventory inv) {
        inv.removeEtc(this);
    }
}
