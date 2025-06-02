import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Journal journal = new Journal();
        FileSystemSimulator fs = new FileSystemSimulator();
        Scanner sc = new Scanner(System.in);

        journal.recovery(fs);

        printStartupMessage();

        while (true) {
            System.out.print(fs.getCurrentPath() + " > ");
            String input = sc.nextLine().trim();

            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String argsPart = (parts.length > 1) ? parts[1] : "";

            switch (command) {
                case "fmk":
                    handleCreateFile(fs, argsPart, journal);
                    break;
                case "fdl":
                    handleDeleteFile(fs, argsPart, sc, journal);
                    break;
                case "frn":
                    handleRenameFile(fs, argsPart, journal);
                    break;
                case "dmk":
                    handleCreateDirectory(fs, argsPart, journal);
                    break;
                case "ddl":
                    handleDeleteDirectory(fs, argsPart, sc, journal);
                    break;
                case "drn":
                    handleRenameDirectory(fs, argsPart, journal);
                    break;
                case "fwrite":
                    handleWriteFile(fs, argsPart, sc, journal);
                    break;
                case "fread":
                    handleReadFile(fs, argsPart);
                    break;
                case "fcpy":
                    handleCopyFile(fs, argsPart);
                    break;
                case "fmv":
                    handleCutFile(fs, argsPart);
                    break;
                case "dcpy":
                    handleCopyDirectory(fs, argsPart);
                    break;
                case "dmv":
                    handleCutDirectory(fs, argsPart);
                    break;
                case "fpaste":
                    handlePasteFile(fs, journal);
                    break;
                case "dpaste":
                    handlePasteDirectory(fs, journal);
                    break;
                case "list":
                    handleListDirectory(fs);
                    break;
                case "go":
                    handleChangeDirectory(fs, argsPart);
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit":
                    handleExit(journal, fs);
                    return;
                case "exit!":
                    handleForceExit();
                    return;
                default:
                    handleUnknownCommand();
            }
        }
    }

    //Funções auxiliares:

    private static boolean confirmInput(Scanner sc) {
        while (true) {
            System.out.print("> ");
            String input = sc.nextLine().trim().toLowerCase();
            if (input.equals("s")) return true;
            if (input.equals("n")) return false;
            System.out.println("Digite 's' para confirmar ou 'n' para cancelar.");
        }
    }

    private static void printStartupMessage() {
        System.out.println("===========================================");
        System.out.println("|      Maia FileSystem Simulator v1.0     |");
        System.out.println("|      © 2025 - Criado por Pedro Maia     |");
        System.out.println("===========================================");
        System.out.println("Digite 'help' para ver os comandos.");
    }

    private static void printHelp() {
        System.out.println("=== COMANDOS DISPONÍVEIS ===");
        System.out.println("  fmk <nome>             - Criar arquivo");
        System.out.println("  dmk <nome>             - Criar diretório");
        System.out.println("  fdl <nome>             - Deletar arquivo (confirmação necessária)");
        System.out.println("  ddl <nome>             - Deletar diretório (confirmação necessária)");
        System.out.println("  frn <nome>             - Renomear arquivo");
        System.out.println("  drn <nome>             - Renomear diretório");
        System.out.println("  fwrite <nome>          - Escrever no arquivo");
        System.out.println("  fread <nome>           - Ler conteúdo do arquivo");
        System.out.println("  fcpy <nome>            - Copiar arquivo");
        System.out.println("  fmv <nome>             - Recortar arquivo");
        System.out.println("  fpaste                 - Colar arquivo");
        System.out.println("  dcpy <nome>            - Copiar diretório");
        System.out.println("  dmv <nome>             - Recortar diretório");
        System.out.println("  dpaste                 - Colar diretório");
        System.out.println("  go <nome|..|path>      - Mudar de diretório");
        System.out.println("                           * Pode usar nome direto (ex: go pasta)");
        System.out.println("                           * '..' para subir");
        System.out.println("                           * Ou caminho (ex: go pasta1/pasta2 ou go /pasta1/pasta2)");
        System.out.println("  list                   - Listar diretórios e arquivos atuais");
        System.out.println("  help                   - Mostrar comandos");
        System.out.println("  exit                   - Salvar e sair");
        System.out.println("  exit!                  - Sair sem salvar");


        System.out.println("===============================");
    }

    //Funções para tratamento de input:
    //Preferi fazer desse jeito para deixar a função main mais limpa e fácil de ler.

    private static void handleCreateFile(FileSystemSimulator fs, String fileName, Journal journal) {
        if (fileName.isEmpty()) {
            System.out.println("ERRO: Nome do arquivo não pode estar vazio.");
            return;
        }
        
        if (fs.makeFile(fileName)) {
            FileType file = fs.getCurrentDir().getFileByName(fileName);
            journal.log("CREATE_FILE", fs.getAbsolutePath(file, fs.getCurrentDir()));
        }
    }

    private static void handleCreateDirectory(FileSystemSimulator fs, String dirName, Journal journal) {
        if (dirName.isEmpty()) {
            System.out.println("ERRO: Nome do diretório não pode estar vazio.");
            return;
        }
        
        if (fs.makeDirectory(dirName)) {
            Directory dir = fs.getCurrentDir().getDirByName(dirName);
            journal.log("CREATE_DIR", fs.getAbsolutePath(dir));
        }
    }

    private static void handleDeleteFile(FileSystemSimulator fs, String fileName, Scanner sc, Journal journal) {
        if (fileName.isEmpty()) {
            System.out.println("ERRO: Nome do arquivo não pode estar vazio.");
            return;
        }
        
        if (!fs.fileExists(fileName)) {
            System.out.println("ERRO: Arquivo não encontrado.");
            return;
        }

        System.out.println("Deseja realmente excluir o arquivo '" + fileName + "'? (s/n)");
        if (confirmInput(sc)) {
            FileType file = fs.getCurrentDir().getFileByName(fileName);
            String filePath = fs.getAbsolutePath(file, fs.getCurrentDir());
            if (fs.deleteFile(fileName)) {
                journal.log("DELETE_FILE", filePath);
                System.out.println("Arquivo excluído com sucesso.");
            }
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    private static void handleDeleteDirectory(FileSystemSimulator fs, String dirName, Scanner sc, Journal journal) {
        if (dirName.isEmpty()) {
            System.out.println("ERRO: Nome do diretório não pode estar vazio.");
            return;
        }
        
        Directory dir = fs.getCurrentDir().getDirByName(dirName);
        if (dir == null) {
            System.out.println("ERRO: Diretório não encontrado.");
            return;
        }

        int subDirs = dir.getSubDirectories().size();
        int files = dir.getFiles().size();
        System.out.println("Deseja realmente excluir o diretório '" + dirName + "'?");
        System.out.println("Ele contém " + subDirs + " subdiretório(s) e " + files + " arquivo(s). (s/n)");
        
        if (confirmInput(sc)) {
            String dirPath = fs.getAbsolutePath(dir);
            if (fs.deleteDirectory(dirName)) {
                journal.log("DELETE_DIR", dirPath);
                System.out.println("Diretório excluído com sucesso.");
            }
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    private static void handleRenameFile(FileSystemSimulator fs, String args, Journal journal) {
        String[] split = args.split("\\s+");
        if (split.length != 2) {
            System.out.println("ERRO: Uso correto: frn <nomeAntigo> <nomeNovo>");
            return;
        }
        
        String oldName = split[0];
        String newName = split[1];
        
        if (!fs.fileExists(oldName)) {
            System.out.println("ERRO: Arquivo '" + oldName + "' não encontrado.");
            return;
        }
        
        FileType file = fs.getCurrentDir().getFileByName(oldName);
        String oldPath = fs.getAbsolutePath(file, fs.getCurrentDir());
        
        if (fs.renameFile(oldName, newName)) {
            String newPath = fs.getAbsolutePath(file, fs.getCurrentDir());
            journal.log("RENAME_FILE", oldPath + " -> " + newPath);
            System.out.println("Arquivo renomeado com sucesso.");
        }
    }

    private static void handleRenameDirectory(FileSystemSimulator fs, String args, Journal journal) {
        String[] split = args.split("\\s+");
        if (split.length != 2) {
            System.out.println("ERRO: Uso correto: drn <nomeAntigo> <nomeNovo>");
            return;
        }
        
        String oldName = split[0];
        String newName = split[1];
        
        if (!fs.dirExists(oldName)) {
            System.out.println("ERRO: Diretório '" + oldName + "' não encontrado.");
            return;
        }
        
        Directory dir = fs.getCurrentDir().getDirByName(oldName);
        String oldPath = fs.getAbsolutePath(dir);
        
        if (fs.renameDirectory(oldName, newName)) {
            String newPath = fs.getAbsolutePath(dir);
            journal.log("RENAME_DIR", oldPath + " -> " + newPath);
            System.out.println("Diretório renomeado com sucesso.");
        }
    }

    private static void handleWriteFile(FileSystemSimulator fs, String fileName, Scanner sc, Journal journal) {
        if (fileName.isEmpty()) {
            System.out.println("ERRO: Nome do arquivo não pode estar vazio.");
            return;
        }
        
        if (!fs.fileExists(fileName)) {
            System.out.println("ERRO: Arquivo não encontrado.");
            return;
        }

        System.out.println("Digite o conteúdo (linha única):");
        System.out.print("> ");
        String content = sc.nextLine();
        
        if (fs.writeFile(fileName, content)) {
            FileType file = fs.getCurrentDir().getFileByName(fileName);
            journal.log("WRITE_FILE", fs.getAbsolutePath(file, fs.getCurrentDir()) + " -> " + content);
            System.out.println("Conteúdo escrito com sucesso.");
        }
    }

    private static void handleReadFile(FileSystemSimulator fs, String fileName) {
        if (fileName.isEmpty()) {
            System.out.println("ERRO: Nome do arquivo não pode estar vazio.");
            return;
        }
        fs.readFile(fileName);
    }

    private static void handleCopyFile(FileSystemSimulator fs, String fileName) {
        if (fileName.isEmpty()) {
            System.out.println("ERRO: Nome do arquivo não pode estar vazio.");
            return;
        }
        fs.copyFile(fileName);
    }

    private static void handleCutFile(FileSystemSimulator fs, String fileName) {
        if (fileName.isEmpty()) {
            System.out.println("ERRO: Nome do arquivo não pode estar vazio.");
            return;
        }
        fs.cutFile(fileName);
    }

    private static void handleCopyDirectory(FileSystemSimulator fs, String dirName) {
        if (dirName.isEmpty()) {
            System.out.println("ERRO: Nome do diretório não pode estar vazio.");
            return;
        }
        fs.copyDirectory(dirName);
    }

    private static void handleCutDirectory(FileSystemSimulator fs, String dirName) {
        if (dirName.isEmpty()) {
            System.out.println("ERRO: Nome do diretório não pode estar vazio.");
            return;
        }
        fs.cutDirectory(dirName);
    }

    private static void handlePasteFile(FileSystemSimulator fs, Journal journal) {
        if (fs.pasteFile()) {
            // Encontrar o arquivo que foi colado (último adicionado)
            if (!fs.getCurrentDir().getFiles().isEmpty()) {
                FileType lastFile = fs.getCurrentDir().getFiles().getFirst();
                journal.log("CREATE_FILE", fs.getAbsolutePath(lastFile, fs.getCurrentDir()));
                System.out.println("Arquivo colado com sucesso.");
            }
        }
    }

    private static void handlePasteDirectory(FileSystemSimulator fs, Journal journal) {
        if (fs.pasteDirectory()) {
            // Encontrar o diretório que foi colado (último adicionado)
            if (!fs.getCurrentDir().getSubDirectories().isEmpty()) {
                Directory lastDir = fs.getCurrentDir().getSubDirectories().getFirst();
                journal.log("CREATE_DIR", fs.getAbsolutePath(lastDir));
                System.out.println("Diretório colado com sucesso.");
            }
        }
    }

    private static void handleListDirectory(FileSystemSimulator fs) {
        fs.listDirectory();
    }

    private static void handleChangeDirectory(FileSystemSimulator fs, String dirName) {
        if (dirName.isEmpty()) {
            System.out.println("ERRO: Nome do diretório não pode estar vazio.");
            return;
        }
        fs.changeDirectory(dirName);
    }

    private static void handleExit(Journal journal, FileSystemSimulator fs) {
        System.out.println("Salvando sistema...");
        journal.close();
        fs.saveFileSystem();
    }

    private static void handleForceExit() {
        System.out.println("Saindo sem salvar...");
    }

    private static void handleUnknownCommand() {
        System.out.println("Comando não reconhecido. Digite 'help' para ver os comandos disponíveis.");
    }
}
