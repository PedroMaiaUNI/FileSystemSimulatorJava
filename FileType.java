import java.time.LocalDateTime;

public class FileType extends Entity{
    private static final long serialVersionUID = 1L;
    
    private String content;
    private LocalDateTime creationDate;

    public FileType(String name) {
        super(name);
        this.creationDate = LocalDateTime.now();
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