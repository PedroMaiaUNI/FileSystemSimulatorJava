import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        FileSystemSimulator fs = new FileSystemSimulator();
        Scanner sc = new Scanner(System.in);

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
                    fs.makeFile(argsPart);
                    break;

                case "fdl":
                    deleteFile(fs, argsPart, sc);
                    break;

                case "frn":
                    renameFile(fs, argsPart, sc);
                    break;

                case "dmk":
                    fs.makeDirectory(argsPart);
                    break;

                case "ddl":
                    deleteDirectory(fs, argsPart, sc);
                    break;
                
                case "drn":
                    renameDirectory(fs, argsPart, sc);
                    break;

                case "fwrite":
                    writeFile(fs, argsPart, sc);
                    break;

                case "fread":
                    fs.readFile(argsPart);
                    break;

                case "fcpy":
                    fs.copyFile(argsPart);
                    break;

                case "fmv":
                    fs.cutFile(argsPart);
                    break;

                case "dcpy":
                    fs.copyDirectory(argsPart);
                    break;

                case "dmv":
                    fs.cutDirectory(argsPart);
                    break;

                case "fpaste":
                    fs.pasteFile();
                    break;

                case "dpaste":
                    fs.pasteDirectory();
                    break;

                case "list":
                    fs.listDirectory();
                    break;

                case "go":
                    fs.changeDirectory(argsPart);
                    break;

                case "help":
                    printHelp();
                    break;

                case "exit":
                    fs.saveFileSystem();
                    return;

                default:
                    System.out.println("Comando não reconhecido. Digite 'help' para ver os comandos.");
            }
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

    System.out.println("===============================");
}


    private static boolean confirmInput(Scanner sc) {
        while (true) {
            System.out.print("> ");
            String input = sc.nextLine().trim().toLowerCase();
            if (input.equals("s")) return true;
            if (input.equals("n")) return false;
            System.out.println("Digite 's' para confirmar ou 'n' para cancelar.");
        }
    }

    private static void deleteFile(FileSystemSimulator fs, String name, Scanner sc) {
        if (fs.fileExists(name)) {
            System.out.println("Deseja realmente excluir o arquivo '" + name + "'? (s/n)");
            if (confirmInput(sc)) {
                fs.deleteFile(name);
            } else {
                System.out.println("Operação cancelada.");
            }
        } else {
            System.out.println("Arquivo não encontrado.");
        }
    }

    private static void deleteDirectory(FileSystemSimulator fs, String name, Scanner sc) {
        Directory dir = fs.getCurrentDir().getDirByName(name);
        if (dir != null) {
            int subDirs = dir.getSubDirectories().size();
            int files = dir.getFiles().size();
            System.out.println("Deseja realmente excluir o diretório '" + name + "'?");
            System.out.println("Ele contém " + subDirs + " subdiretório(s) e " + files + " arquivo(s). (s/n)");
            if (confirmInput(sc)) {
                fs.deleteDirectory(name);
            } else {
                System.out.println("Operação cancelada.");
            }
        } else {
            System.out.println("Diretório não encontrado.");
        }
    }

    private static void writeFile(FileSystemSimulator fs, String fileName, Scanner sc) {
        if (fs.fileExists(fileName)) {
            System.out.println("Digite o conteúdo (linha única):");
            System.out.print("> ");
            String content = sc.nextLine();
            fs.writeFile(fileName, content);
        } else {
            System.out.println("Arquivo não encontrado.");
        }
    }

    private static void renameFile(FileSystemSimulator fs, String args, Scanner sc) {
        String[] split = args.split("\\s+");
        if (split.length != 2) {
            System.out.println("Uso correto: renamefile <nomeAntigo> <nomeNovo>");
            return;
        }
        fs.renameFile(split[0], split[1]);
    }

    private static void renameDirectory(FileSystemSimulator fs, String args, Scanner sc) {
        String[] split = args.split("\\s+");
        if (split.length != 2) {
            System.out.println("Uso correto: renamedir <nomeAntigo> <nomeNovo>");
            return;
        }
        fs.renameDirectory(split[0], split[1]);
    }
}
