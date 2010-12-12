package common.items;

import common.Inventory;
import common.Item;
import common.Message;
import common.messages.AddEtcItem;
import common.messages.InventoryAddEtcItem;
import common.messages.InventoryRemoveEtcItem;

/**
 * Прочий предмет. Например: золото, кусок металла, пробка. Все предметы, для
 * которых нет (пока?) обобщения.
 * @author CatsPaw
 */
public class Etc extends Item {

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

    /**
     * Returns message notify user what etc item added to his inventory.
     * @return message what notify user what etc item added to his inventory.
     */
    public Message getAddToInvenrotyMessage() {
        return new InventoryAddEtcItem(getID(), getCount(), getType());
    }

    /**
     * Returns message notify user what etc item removed from his inventory.
     * @return message waht notify user what etc item removed from his inventory.
     */
    public Message getRemoveFromInventoryMessage() {
        return new InventoryRemoveEtcItem(getID(), getCount(), getType());
    }

    /**
     * Returns message notify user what etc item dropped to the ground.
     * @return message what notify user what etc item dropped to the ground.
     */
    public Message getDropMessage() {
        return new AddEtcItem(getID(), getX(), getY(), getCount(), getType());
    }
}
