public class File {
    protected String name;
    private String content;

    public File(String name) {
        setName(name);
    }

    public String getExtension(){
        return this.name.substring(this.name.lastIndexOf("."));
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setName(String name) {
        if(!name.contains(".")){
            System.out.println("Adicione uma extensão ao arquivo com um ''.'' !");
            return;
        }
        this.name = name;
    }
}