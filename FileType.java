import java.time.LocalDateTime;

public class FileType extends Entity{
    private static final long serialVersionUID = 1L;
    
    private String content;
    private LocalDateTime creationDate;

    private int size = 1;

    public FileType(String name) {
        super(name);
        this.creationDate = LocalDateTime.now();
    }

    public void setMainName(String mainName){
        setName(mainName + "." + getExtension().toLowerCase());
    }

    public String getMainName(){
        return this.name.substring(0, getName().lastIndexOf("."));
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.size = content != null ? content.length() + 1 : 1; 
        this.content = content;
    }

    public int getSize() {
        return size;
    }

    public FileType deepCopy() {
        FileType copy = new FileType(this.getName());
        copy.setContent(this.getContent());
        return copy;
    }
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public String getExtension() {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < name.length() - 1) {
            return name.substring(dotIndex + 1).toUpperCase();
        }
        return "";
    }

}