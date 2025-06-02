public class FileType extends Entity{
    private static final long serialVersionUID = 1L;
    
    private String content;

    public FileType(String name) {
        super(name);
    }

    public String getExtension(){
        return this.name.substring(this.name.lastIndexOf("."));
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public FileType deepCopy() {
        FileType copy = new FileType(this.getName());
        copy.setContent(this.getContent());
        return copy;
    }

}