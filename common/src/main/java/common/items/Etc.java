package common.items;

import java.util.Objects;

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
     * @param count начальное положительное количество.
     */
    protected Etc(long id, String name, Items type, int count) {
        super(id, name, count);
        this.type = Objects.requireNonNull(type);
    }

    /**
     * Возвращает тип прочего предмета.
     * @return тип прочего предмета.
     */
    public Items getType() {
        return type;
    }

}
