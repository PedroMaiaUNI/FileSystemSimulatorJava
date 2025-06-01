public abstract class Entity {
    protected String name;

    public Entity(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name == null || name.isBlank()) {
            System.out.println("Nome não pode ser vazio.");
            return;
        }
        this.name = name;
    }
}
