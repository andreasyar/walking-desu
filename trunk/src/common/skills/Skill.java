package common.skills;

/**
 *
 * @author sorc
 */
public class Skill {

    private long id;
    private String name;
    private SkillType type;

    public Skill(long id, String name, SkillType type) {
        this.id = id;
        this.name = name;
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
}
