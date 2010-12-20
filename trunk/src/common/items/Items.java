package common.items;

/**
 * Items types.
 * @author CatsPaw
 */
public enum Items {

    // Etc.
    GOLD("Gold");

    private String customName;

    Items(String customName) {
        this.customName = customName;
    }

    public String getCustomName() {
        return customName;
    }
}
