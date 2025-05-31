import java.util.LinkedList;

public class Directory extends File{

    private LinkedList<File> files;
    private LinkedList<Directory> subDirectories;

    public Directory(String name) {
        super(name);

        this.files = new LinkedList<>();
        this.subDirectories = new LinkedList<>();
    }

    public LinkedList<File> getFiles() {
        return files;
    }

    public void setFiles(File file) {
        files.push(file);
    }

    public LinkedList<Directory> getSubDirectories() {
        return subDirectories;
    }

    public void setSubDirectories(Directory subDirectory) {
        subDirectories.push(subDirectory);
    }

    @Override
    public void setName(String name){
        this.name = name;
    }
}