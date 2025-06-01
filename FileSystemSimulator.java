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

    public String getCurrentPath() {
        return getPath(currentDir);
    }
    private String getPath(Directory dir) {
        if (dir.getParent() == null) {
            return "/" + dir.getName();
        }
        //mini-recursão até chegar no root
        return getPath(dir.getParent()) + "/" + dir.getName();
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
    public void makeFile(String fileName) {
        if (!isNameValid(fileName)) return;
        if (fileExists(fileName)) {
            System.out.println("ERRO: Já existe um arquivo com esse nome.");
            return; 
        }
        currentDir.addFiles(new FileType(fileName));
    }

    public void deleteFile(String fileName){
        currentDir.removeFileByName(fileName);

    }public void renameFile(String fileName, String newName) {
        FileType file = currentDir.getFileByName(fileName);
        if (file == null) {
            System.out.println("Não existe um arquivo com esse nome.");
            return;
        }
        if (!isNameValid(newName)) return;
        if (fileExists(newName)) {
            System.out.println("ERRO: Já existe um arquivo com esse nome.");
            return;
        }
        file.setName(newName);
    }

    public void copyFile(String fileName){
        if (fileExists(fileName)){
            transferFile = currentDir.getFileByName(fileName).deepCopy();
        } else {
            System.out.println("Não existe arquivo com esse nome");
        }
    }

    public void cutFile(String fileName){
        copyFile(fileName);
        deleteFile(fileName);
    }

    public void pasteFile(){
        if(transferFile != null){
            String newName = transferFile.getName();
            //quando eu usei ubuntu, era mais ou menos desse jeito
            while(fileExists(newName)){
                newName += "(cópia)";
                if(!isNameValid(newName)){
                    return;
                }
            }
            FileType copy = transferFile.deepCopy();
            copy.setName(newName);
            currentDir.addFiles(copy);
        } else {
            System.out.println("A área de transferência está vazia!");
        }     
    }

    public void writeFile(String name, String content) {
        FileType file = currentDir.getFileByName(name);
        if (file != null) {
            file.setContent(content);
        } else {
            System.out.println("Arquivo não encontrado.");
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

    //Diretorio:
    public void makeDirectory(String dirName) {
        if (!isNameValid(dirName)) return;
        if (dirExists(dirName)) {
            System.out.println("ERRO: Já existe um diretório com esse nome.");
            return;
        }
        Directory dir = new Directory(dirName);
        dir.setParent(currentDir);
        currentDir.addSubDirectories(dir);
    }

    public void deleteDirectory(String dirName){
        currentDir.removeDirByName(dirName);
    }

    public void renameDirectory(String dirName, String newName) {
        Directory dir = currentDir.getDirByName(dirName);
        if (dir == null) {
            System.out.println("ERRO: Não existe um diretório com esse nome.");
            return;
        }
        if (!isNameValid(newName)) return;
        if (dirExists(newName)) {
            System.out.println("ERRO: Já existe um diretório com esse nome.");
            return;
        }
        dir.setName(newName);
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

    public void pasteDirectory() {
        if (transferDir != null) {
            String newName = transferDir.getName();
            while (dirExists(newName)) {
                newName += "(cópia)";
                if(!isNameValid(newName)){
                    return;
                }
            }
            Directory copy = transferDir.deepCopy();
            copy.setName(newName);
            copy.setParent(currentDir);
            currentDir.addSubDirectories(copy);
        } else {
            System.out.println("A área de transferência está vazia!");
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
                        // no cmd do windows nenhum erro é retornado, então deixarei assim
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
            // voltando ao diretorio-pai
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
    //se isso estiver vazio depois, desculpa...
}
