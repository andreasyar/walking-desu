package common.skills;

public class Skill {

    private long id;
    private String name;
    private final long reuse;
    private SkillType type;

    public Skill(long id, String name, long reuse, SkillType type) {
        this.id = id;
        this.name = name;
        this.reuse = reuse;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SkillType getType() {
        return type;
    }

    public long getReuse() {
        return reuse;
    }
}
