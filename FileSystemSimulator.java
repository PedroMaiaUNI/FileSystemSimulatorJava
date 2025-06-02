import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileSystemSimulator {
    private static final int MAX_PATH_LENGTH = 300;
    private static final String SAVE_FILE = "filesystem.dat";

    private FileType transferFile = null;
    private Directory transferDir = null;

    private Directory root;
    private Directory currentDir;

    public FileSystemSimulator() {
        root = loadFileSystem();
        if (root == null) {
            root = new Directory("root");
        }
        currentDir = root;
    }

    public Directory getCurrentDir() {
        return currentDir;
    }

    // FUNÇÕES AUXILIARES:

    public boolean fileExists(String name) {
        return currentDir.getFileByName(name) != null;
    }
    public boolean dirExists(String name) {
        return currentDir.getDirByName(name) != null;
    }

    //para diretorio
    public String getAbsolutePath(Directory dir) {
        return getPath(dir);
    }

    //para arquivo
    public String getAbsolutePath(FileType file, Directory parentDir) {
        return getPath(parentDir) + "/" + file.getName();
    }

    public String getCurrentPath() {
        return getPath(currentDir);
    }

    //função recursiva de montagem do path
    private String getPath(Directory dir) {
        if (dir.getParent() == null) {
            return "/";
        }
        String parentPath = getPath(dir.getParent());
        return parentPath.equals("/") ? "/" + dir.getName() : parentPath + "/" + dir.getName();
    }


    private boolean isNameValid(String name) {
        if (name.contains("/") || name.contains("\\") || name.equals("..")) {
            System.out.println("ERRO: Caracteres inválidos ('/'. '\\' ou '..').");
            return false;
        }
        // +5 pelo root/
        int pathLength = getCurrentPath().length() + name.length() + 5; 
        if (pathLength > MAX_PATH_LENGTH) {
            System.out.println("ERRO: O caminho completo excede o limite de " + MAX_PATH_LENGTH + " caracteres.");
            return false;
        }
        return true;
    }
    
    // FUNÇÕES ESSENCIAIS:

    //Arquivo:
    public boolean makeFile(String fileName) {
        if (!isNameValid(fileName)) return false;
        if (fileExists(fileName)) {
            System.out.println("ERRO: Já existe um arquivo com esse nome.");
            return false; 
        }
        FileType file = new FileType(fileName);
        currentDir.addFiles(file);
        return true;
    }

    public boolean deleteFile(String fileName) {
        FileType file = currentDir.getFileByName(fileName);
        if (file != null) {
            currentDir.removeFileByName(fileName);
            return true;
        }
        return false;
    }

    public boolean renameFile(String fileName, String newName) {
        FileType file = currentDir.getFileByName(fileName);
        if (file == null) {
            System.out.println("ERRO: Não existe um arquivo com esse nome.");
            return false;
        }
        if (!isNameValid(newName)) return false;
        if (fileExists(newName)) {
            System.out.println("ERRO: Já existe um arquivo com esse nome.");
            return false;
        }
        file.setName(newName);
        return true;
    }

    public void copyFile(String fileName){
        if (fileExists(fileName)){
            transferFile = currentDir.getFileByName(fileName).deepCopy();
        } else {
            System.out.println("ERRO: Não existe arquivo com esse nome");
        }
    }

    public void cutFile(String fileName){
        copyFile(fileName);
        deleteFile(fileName);
    }

    public boolean pasteFile(){
        if(transferFile != null){
            String newName = transferFile.getName();
            while(fileExists(newName)){
                newName += "(cópia)";
                if(!isNameValid(newName)){
                    return false;
                }
            }
            FileType copy = transferFile.deepCopy();
            copy.setName(newName);
            currentDir.addFiles(copy);
            return true;
        } else {
            System.out.println("A área de transferência está vazia!");
            return false;
        }     
    }

    //Diretorio:
    public boolean makeDirectory(String dirName) {
        if (!isNameValid(dirName)) return false;
        if (dirExists(dirName)) {
            System.out.println("ERRO: Já existe um diretório com esse nome.");
            return false;
        }
        Directory dir = new Directory(dirName);
        dir.setParent(currentDir);
        currentDir.addSubDirectories(dir);
        return true;
    }

    public boolean deleteDirectory(String dirName) {
        Directory dir = currentDir.getDirByName(dirName);
        if (dir != null) {
            currentDir.removeDirByName(dirName);
            return true;
        }
        return false;
    }

    public boolean renameDirectory(String dirName, String newName) {
        Directory dir = currentDir.getDirByName(dirName);
        if (dir == null) {
            System.out.println("ERRO: Não existe um diretório com esse nome.");
            return false;
        }
        if (!isNameValid(newName)) return false;
        if (dirExists(newName)) {
            System.out.println("ERRO: Já existe um diretório com esse nome.");
            return false;
        }
        dir.setName(newName);
        return true;
    }

    public void copyDirectory(String dirName){
        if (dirExists(dirName)){
            transferDir = currentDir.getDirByName(dirName).deepCopy();
        } else {
            System.out.println("Não existe diretório com esse nome");
        }
    }

    public void cutDirectory(String dirName){
        copyDirectory(dirName);
        deleteDirectory(dirName);
    }

    public boolean pasteDirectory() {
        if (transferDir != null) {
            String newName = transferDir.getName();
            while (dirExists(newName)) {
                newName += "(cópia)";
                if(!isNameValid(newName)){
                    return false;
                }
            }
            Directory copy = transferDir.deepCopy();
            copy.setName(newName);
            copy.setParent(currentDir);
            currentDir.addSubDirectories(copy);
            return true;
        } else {
            System.out.println("A área de transferência está vazia!");
            return false;
        }
    }

    public void listDirectory(){
        for (Directory dir : currentDir.getSubDirectories()) {
            System.out.println("[DIR]  " + dir.getName() + "/ (Subdiretorios: " + dir.getSubDirectories().size() + ", Arquivos: " + dir.getFiles().size() + ")");
        }
        for (FileType file : currentDir.getFiles()) {
            System.out.println("[FILE] " + file.getName() + " (Tamanho: " + (file.getContent() == null ? 0 : file.getContent().length()) + " caracteres)");
        }
    }

    public void changeDirectory(String name) {
        if (name == null || name.isEmpty()) {
            System.out.println("Informe um nome ou caminho válido.");
            return;
        }

        if (name.contains("/")) {
            Directory startDir;
            if (name.startsWith("/")) {
                // desde a root
                startDir = root;
                name = name.substring(1);
            } else {
                // desde o diretorio atual
                startDir = currentDir;
            }

            String[] parts = name.split("/");

            Directory tempDir = startDir;

            for (String part : parts) {
                if (part.equals("") || part.equals(".")) {
                    continue;
                } else if (part.equals("..")) {
                    if (tempDir.getParent() != null) {
                        tempDir = tempDir.getParent();
                    } else {
                        return;
                    }
                } else {
                    Directory nextDir = tempDir.getDirByName(part);
                    if (nextDir == null) {
                        System.out.println("Diretório não encontrado: " + part);
                        return;
                    } else {
                        tempDir = nextDir;
                    }
                }
            }
            currentDir = tempDir;
        } else {
            if (name.equals("..")) {
                if (currentDir.getParent() != null) {
                    currentDir = currentDir.getParent();
                }
            } else {
                Directory dir = currentDir.getDirByName(name);
                if (dir != null) {
                    currentDir = dir;
                } else {
                    System.out.println("Diretório não encontrado. Utilize 'list' para visualizar os diretórios.");
                }
            }
        }
    }



    //funções para manipular o arquivo (salvar e carregar):

    public void saveFileSystem() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(root);
            System.out.println("Sistema salvo. Encerrando...");
        } catch (IOException e) {
            System.out.println("Erro ao salvar: " + e.getMessage());
        }
    }

    private Directory loadFileSystem() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            return (Directory) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Sistema está limpo. Criando diretório raiz...");
            return new Directory("root");
        }
    }


    //OUTRAS FUNCIONALIDADES:

    //por enquanto bem básico, ela só existe mesmo
    public boolean writeFile(String name, String content) {
        FileType file = currentDir.getFileByName(name);
        if (file != null) {
            file.setContent(content);
            return true;
        } else {
            System.out.println("Arquivo não encontrado.");
            return false;
        }
    }

    public void readFile(String name) {
        FileType file = currentDir.getFileByName(name);
        if (file != null) {
            System.out.println(file.getContent() == null ? "(vazio)" : file.getContent());
        } else {
            System.out.println("Arquivo não encontrado.");
        }
    }

}
