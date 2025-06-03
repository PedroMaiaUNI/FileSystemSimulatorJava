import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Journal journal = new Journal();
        FileSystemSimulator fs = new FileSystemSimulator();
        Scanner sc = new Scanner(System.in);

        journal.recovery(fs);

        //zerar o clipboard foi mais uma opção que um bug
        //não faz muito sentido pra mim isso ser salvo 
        //se nem no ruindows isso acontece
        fs.clipboardStatus = 0;

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
                case "dmk":
                    handleCreateDirectory(fs, argsPart, journal);
                    break;
                case "ren":
                    handleRename(fs, argsPart, journal);
                    break;
                case "del":
                    handleDelete(fs, argsPart, sc, journal);
                    break;
                case "fwrite":
                    handleWriteFile(fs, argsPart, sc, journal);
                    break;
                case "fread":
                    handleReadFile(fs, argsPart);
                    break;
                case "cpy":
                    handleCopy(fs, argsPart, journal);
                    break;
                case "mv":
                    handleCut(fs, argsPart);
                    break;
                case "paste":
                    handlePaste(fs, sc, journal);
                    break;
                case "dup":
                    handleDuplicate(fs, argsPart, journal);
                    break;
                case "list":
                    handleListDirectory(fs);
                    break;
                case "go":
                    handleChangeDirectory(fs, argsPart, journal);
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit":
                    handleExit(journal, fs);
                    journal.clear();
                    return;
                case "exit!":
                    handleForceExit();
                    journal.clear();
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
        System.out.println("  del <nome>             - Deletar arquivo ou diretório (confirmação necessária)");
        System.out.println("  ren <nome>             - Renomear arquivo ou diretório");
        System.out.println("  fwrite <nome>          - Escrever no arquivo");
        System.out.println("  fread <nome>           - Ler conteúdo do arquivo");
        System.out.println("  cpy <nome>             - Copiar arquivo ou diretório");
        System.out.println("  mv <nome>              - Recortar arquivo ou diretório");
        System.out.println("  paste                  - Colar da área de transferência");
        System.out.println("  go <nome|..|path>      - Mudar de diretório");
        System.out.println("                           * Pode usar nome direto (ex: go pasta)");
        System.out.println("                           * '..' para subir");
        System.out.println("                           * Ou path (ex: go pasta1/pasta2 ou go /pasta1/pasta2)");
        System.out.println("                           * (ATENÇÃO: quando o path inicia com '/', é implicito que a barra é o root)");
        System.out.println("  list                   - Listar diretórios e arquivos atuais");
        System.out.println("  help                   - Mostrar comandos");
        System.out.println("  exit                   - Salvar e sair");
        System.out.println("  exit!                  - Sair sem salvar");


        System.out.println("===============================");
    }

    //Funções para tratamento de input:
    //Preferi fazer desse jeito para deixar a função main mais limpa e fácil de ler

    private static void handleCreateFile(FileSystemSimulator fs, String fileName, Journal journal) {
        if (fileName.isEmpty()) {
            System.out.println("ERRO: Nome do arquivo não pode estar vazio.");
            return;
        }
        
        if (fs.makeFile(fileName)) {
            journal.log("CREATE_FILE", fileName);
        }
    }

    private static void handleCreateDirectory(FileSystemSimulator fs, String dirName, Journal journal) {
        if (dirName.isEmpty()) {
            System.out.println("ERRO: Nome do diretório não pode estar vazio.");
            return;
        }
        if (fs.makeDirectory(dirName)) {
            journal.log("CREATE_DIR", dirName);
        }
    }

    private static void handleDelete(FileSystemSimulator fs, String name, Scanner sc, Journal journal) {
        if (name.isEmpty()) {
            System.out.println("ERRO: Nome não pode estar vazio.");
            return;
        }

        if (fs.fileExists(name)) {
            System.out.println("Deseja realmente excluir o arquivo '" + name + "'? (s/n)");
            if (confirmInput(sc)) {
                if (fs.deleteFile(name)) {
                    journal.log("DELETE_FILE", name);
                    System.out.println("Arquivo excluído com sucesso.");
                }
            } else {
                System.out.println("Operação cancelada.");
            }

        } else if (fs.dirExists(name)) {
            Directory dir = fs.getCurrentDir().getDirByName(name);
            int subDirs = dir.getSubDirectories().size();
            int files = dir.getFiles().size();
            System.out.println("Deseja realmente excluir o diretório '" + name + "'?");
            System.out.println("Ele contém " + subDirs + " subdiretório(s) e " + files + " arquivo(s). (s/n)");

            if (confirmInput(sc)) {
                if (fs.deleteDirectory(name)) {
                    journal.log("DELETE_DIR", name);
                    System.out.println("Diretório excluído com sucesso.");
                }
            } else {
                System.out.println("Operação cancelada.");
            }

        } else {
            System.out.println("ERRO: Arquivo ou diretório '" + name + "' não encontrado.");
        }
    }


    private static void handleRename(FileSystemSimulator fs, String args, Journal journal) {
        String[] split = args.split("\\s+");
        if (split.length != 2) {
            System.out.println("ERRO: Uso correto: ren <nomeAntigo> <nomeNovo>");
            return;
        }

        String oldName = split[0];
        String newName = split[1];

        if (fs.fileExists(oldName)) {

            if (fs.renameFile(oldName, newName)) {
                journal.log("RENAME_FILE", oldName + " -> " + newName);
                System.out.println("Arquivo renomeado com sucesso.");
            }

        } else if (fs.dirExists(oldName)) {

            if (fs.renameDirectory(oldName, newName)) {
                journal.log("RENAME_DIR", oldName + " -> " + newName);
                System.out.println("Diretório renomeado com sucesso.");
            }

        } else {
            System.out.println("ERRO: Arquivo ou diretório '" + oldName + "' não encontrado.");
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
            journal.log("WRITE_FILE", fileName + " -> " + content);
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

    private static void handleCopy(FileSystemSimulator fs, String name, Journal journal) {
        if (name.isEmpty()) {
            System.out.println("ERRO: Nome não pode estar vazio.");
            return;
        }

        if (fs.fileExists(name)) {
            fs.copyFile(name);
            journal.log("COPY_FILE", name);
            System.out.println("Arquivo '" + name + "' copiado para a área de transferência.");
            return;
        } else if (fs.dirExists(name)) {
            fs.copyDirectory(name);
            journal.log("COPY_DIR", name);
            System.out.println("Diretório '" + name + "' copiado para a área de transferência.");
            return;
        } else {
            System.out.println("ERRO: Arquivo ou diretório '" + name + "' não encontrado.");
        }
    }

    private static void handleCut(FileSystemSimulator fs, String name) {
        if (name.isEmpty()) {
            System.out.println("ERRO: Nome não pode estar vazio.");
            return;
        }

        if (fs.fileExists(name)) {
            fs.cutFile(name);
            System.out.println("Arquivo '" + name + "' recortado para a área de transferência.");
        } else if (fs.dirExists(name)) {
            fs.cutDirectory(name);
            System.out.println("Diretório '" + name + "' recortado para a área de transferência.");
        } else {
            System.out.println("ERRO: Arquivo ou diretório '" + name + "' não encontrado.");
        }
    }

    private static void handlePaste(FileSystemSimulator fs, Scanner sc, Journal journal) {
        if(fs.clipboardStatus == 1){
            handlePasteFile(fs, sc, journal);
            return;
        }
        if(fs.clipboardStatus == 2){
            handlePasteDirectory(fs, sc, journal);
        }
        else{
            System.out.println("A área de transferência está vazia!");
        }
    }

    private static void handlePasteFile(FileSystemSimulator fs, Scanner sc, Journal journal) {
        String transferName = fs.getTransferFile().name;
        if (fs.fileExists(transferName)) {
                System.out.println("Já existe um arquivo chamado '" + transferName + "'. Deseja sobrescrever? (s/n)");
            if (!confirmInput(sc)) {
                System.out.println("Operação cancelada.");
                return;
            }
            journal.log("DELETE_FILE", transferName);
        }
        if (fs.pasteFile()) {
            journal.log("PASTE_FILE", transferName);
            System.out.println("Arquivo colado com sucesso.");
        }
    }

    private static void handlePasteDirectory(FileSystemSimulator fs,  Scanner sc, Journal journal) {
        String transferName = fs.getTransferDir().getName();
        if (fs.dirExists(transferName)) {
            System.out.println("Já existe um diretório chamado '" + transferName + "'. Deseja sobrescrever? (s/n)");
            if (!confirmInput(sc)) {
                System.out.println("Operação cancelada.");
                return;
            }
            journal.log("DELETE_DIR", transferName);
        }
        if (fs.pasteDirectory()) {
            journal.log("PASTE_DIR", transferName);
            System.out.println("Diretório colado com sucesso.");
        }
    }

    private static void handleDuplicate(FileSystemSimulator fs, String name, Journal journal) {
        if (name.isEmpty()) {
            System.out.println("ERRO: Nome não pode estar vazio.");
            return;
        }

        if (fs.fileExists(name)) {
            FileType duplicated = fs.duplicateFile(name);
            if (duplicated != null) {
                journal.log("DUPLICATE_FILE", duplicated.getName());
                System.out.println("Arquivo duplicado como '" + duplicated.getName() + "'.");
            }
        } else if (fs.dirExists(name)) {
            Directory duplicated = fs.duplicateDirectory(name);
            if (duplicated != null) {
                journal.log("DUPLICATE_DIR", duplicated.getName());
                System.out.println("Diretório duplicado como '" + duplicated.getName() + "'.");
            }
        } else {
            System.out.println("ERRO: Arquivo ou diretório '" + name + "' não encontrado.");
        }
    }

    private static void handleListDirectory(FileSystemSimulator fs) {
        fs.listDirectory();
    }

    private static void handleChangeDirectory(FileSystemSimulator fs, String dirName, Journal journal) {
        if (dirName.isEmpty()) {
            System.out.println("ERRO: Nome do diretório não pode estar vazio.");
            return;
        }
        journal.log("CHANGE_DIR", dirName);
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
