import java.io.Serializable;
import java.util.LinkedList;

public class Directory extends Entity implements Serializable{
    private static final long serialVersionUID = 1L;

    private LinkedList<FileType> files;
    private LinkedList<Directory> subDirectories;
    private Directory parentDirectory;

    public Directory(String name) {
        super(name);

        this.files = new LinkedList<>();
        this.subDirectories = new LinkedList<>();
    }

    public LinkedList<FileType> getFiles() {
        return files;
    }

    public void addFiles(FileType file) {
        files.push(file);
    }

    public LinkedList<Directory> getSubDirectories() {
        return subDirectories;
    }

    public void addSubDirectories(Directory subDirectory) {
        subDirectories.push(subDirectory);
    }

    public Directory getParent() {
        return parentDirectory;
    }   

    public void setParent(Directory parentDir) {
        this.parentDirectory = parentDir;
    }

    public FileType getFileByName(String fileName){
        FileType returnFile = null;
        for(FileType file : files){
            if(file.getName().equals(fileName)){
                returnFile = file;
                break;
            }
        }
        return returnFile;
    }

    public void removeFileByName(String fileName){
        FileType removeFile = null;
        for(FileType file : files){
            if(file.getName().equals(fileName)){
                removeFile = file;
                break;
            }
        }
        if(removeFile == null){
            System.out.println("Arquivo não encontrado\n");
            return;
        }
        files.remove(removeFile);
    }

    public Directory getDirByName(String dirName){
        Directory returnDir = null;
        for(Directory dir : subDirectories){
            if(dir.getName().equals(dirName)){
                returnDir = dir;
                break;
            }
        }
        return returnDir;
    }

    public void removeDirByName(String dirName){
        Directory removeDir = null;
        for(Directory dir : subDirectories){
            if(dir.getName().equals(dirName)){
                removeDir = dir;
                break;
            }
        }
        if(removeDir == null){
            System.out.println("Diretório não encontrado\n");
            return;
        }
        subDirectories.remove(removeDir);
    }

    public Directory deepCopy() {
        Directory copy = new Directory(this.getName());
        
        for (FileType file : this.getFiles()) {
            copy.addFiles(file.deepCopy());
        }
        
        //copiando recursivamente
        for (Directory subDir : this.getSubDirectories()) {
            Directory subDirCopy = subDir.deepCopy();
            subDirCopy.setParent(copy); 
            copy.addSubDirectories(subDirCopy);
        }

        return copy;
    }
}