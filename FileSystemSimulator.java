import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.format.DateTimeFormatter;

public class FileSystemSimulator {
    private static final int MAX_PATH_LENGTH = 300;
    private static final String SAVE_FILE = "filesystem.dat";

    public int clipboardStatus = 0;
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

    public FileType getTransferFile() {
        return transferFile;
    }

    public Directory getTransferDir() {
        return transferDir;
    }

    // FUNÇÕES AUXILIARES:

    public boolean fileExists(String name) {
        return currentDir.getFileByName(name) != null;
    }
    public boolean dirExists(String name) {
        return currentDir.getDirByName(name) != null;
    }

    public boolean hasExtension(String fileName) {
        //verificar se tem extensão, vai basicamente só ver se tem um ponto separando nome e extensão
        //se não tiver é erro
        if (fileName == null) return false;
        int dotIndex = fileName.lastIndexOf('.');
        //por segurança, não pode ser o ultimo caractere
        return dotIndex > 0 && dotIndex < fileName.length() - 1;
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
        if (name.contains("/") || name.contains("\\") || name.contains("..")) {
            System.out.println("ERRO: Caracteres inválidos ('/'. '\\', ou '..')");
            return false;
        }
        int pathLength = getCurrentPath().length() + name.length() + 1; 
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
        if (!hasExtension(fileName)) {
            System.out.println("ERRO: O nome do arquivo deve conter uma extensão.");
            return false;
        }
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
        if (!hasExtension(newName)) {
            System.out.println("ERRO: O novo nome do arquivo deve conter uma extensão.");
            return false;
        }
        if (fileExists(newName)) {
            System.out.println("ERRO: Já existe um arquivo com esse nome.");
            return false;
        }
        file.setName(newName);
        return true;
    }

    public void copyFile(String fileName){
        if (fileExists(fileName)){
            clipboardStatus = 1;
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
            if(fileExists(newName)){
                deleteFile(newName);
            }
            FileType copy = transferFile.deepCopy();
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
            clipboardStatus = 2;
            transferDir = currentDir.getDirByName(dirName);
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
            if(dirExists(newName)) {
                deleteDirectory(newName);
            }
            Directory copy = transferDir.deepCopy();
            copy.setParent(currentDir);
            currentDir.addSubDirectories(copy);
            return true;
        } else {
            System.out.println("A área de transferência está vazia!");
            return false;
        }
    }

    public void listDirectory() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        System.out.println("----------------------------------------------------------------------------");
        System.out.printf("%-10s | %-25s | %-10s | %-20s\n", "", "NOME", "EXTENSÃO", "DATA DE CRIAÇÃO");
        System.out.println("----------------------------------------------------------------------------");

        for (Directory subDir : currentDir.getSubDirectories()) {
            System.out.printf("%-10s | %-25s | %-10s | %-20s\n",
                "[DIR]",
                subDir.getName(),
                "",
                subDir.getCreationDate().format(formatter));
        }

        for (FileType file : currentDir.getFiles()) {
            String ext = file.getExtension();
            System.out.printf("%-10s | %-25s | %-10s | %-20s\n",
                 "[FILE]",
                file.getName(), // TODO: REMOVER EXTENSÃO
                ext,
                file.getCreationDate().format(formatter));
        }

        System.out.println("----------------------------------------------------------------------------");
    }

    public void changeDirectory(String name) {
        if (name == null || name.isEmpty()) {
            System.out.println("Informe um nome ou caminho válido.");
            return;
        }

        if (name.contains("/")) {
            //tratando de buscas que incluem path
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
            //principal razão de getDirByName não ser deepCopy mas sim referencia:
            currentDir = tempDir;
        } else {
            //voltando ao diretorio-pai, padrao windows
            if (name.equals("..")) {
                if (currentDir.getParent() != null) {
                    currentDir = currentDir.getParent();
                }
            } else {
            //buscando nos subdiretorios
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
        FileType fileToWrite = null;
        for(FileType file : currentDir.getFiles()){
            if(file.getName().equals(name)){
                fileToWrite = file;
            }
        }
        if (fileToWrite != null) {
            fileToWrite.setContent(content);
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

    //duplicar, para quando o usuario realmente quiser uma copia
    public FileType duplicateFile(String fileName) {
        FileType dupFile = currentDir.getFileByName(fileName);
        if (dupFile == null) {
            System.out.println("ERRO: Arquivo não encontrado.");
        }
        int copyCount = 0;
        for(FileType file : currentDir.getFiles()){
            if(file.getName().equals(fileName)){
                copyCount++;
            }
        }
        if(copyCount > 0){
            dupFile.setName(dupFile.getName() + "(" + copyCount + ")");
            currentDir.addFiles(dupFile);
        }
        return dupFile;
    }

    public Directory duplicateDirectory(String dirName) {
        Directory dupDir = currentDir.getDirByName(dirName).deepCopy();
        if (dupDir == null) {
            System.out.println("ERRO: Diretorio não encontrado.");
        }
        int copyCount = 0;
        for(Directory dir : currentDir.getSubDirectories()){
            if(dir.getName().equals(dirName)){
                copyCount++;
            }
        }
        if(copyCount > 0){
            dupDir.setName(dupDir.getName() + "(" + copyCount + ")");
            dupDir.setParent(currentDir);
            currentDir.addSubDirectories(dupDir);
        }
        return dupDir;
    }
}
