package newcommon;

public abstract class Entity {

    private final long id;
    private String name;

    protected Entity(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
