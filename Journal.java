import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Journal {
    private static final String JOURNAL_FILE = "journal.log";
    private BufferedWriter writer;

    public Journal() {
        try {
            writer = new BufferedWriter(new FileWriter(JOURNAL_FILE, true));
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o journal: " + e.getMessage());
        }
    }

    public void log(String action, String details) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(String.format("[%s] %s | %s\n", timestamp, action, details));
            writer.flush();
        } catch (IOException e) {
            System.out.println("Erro ao escrever no journal: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            System.out.println("Erro ao fechar o journal: " + e.getMessage());
        }
    }

    public void clear() {
        try {
            writer.close();
            new FileWriter(JOURNAL_FILE, false).close();
            writer = new BufferedWriter(new FileWriter(JOURNAL_FILE, true));
        } catch (IOException e) {
            System.out.println("Erro ao limpar o journal: " + e.getMessage());
        }
    }

    public void recovery(FileSystemSimulator fs) {
        try (BufferedReader reader = new BufferedReader(new FileReader(JOURNAL_FILE))) {
            String line;
            System.out.println("Iniciando recuperação via journal...");
            while ((line = reader.readLine()) != null) {
                processLogLine(fs, line);
            }
            System.out.println("Recuperação finalizada.");
        } catch (IOException e) {
            System.out.println("Nenhum journal encontrado para recuperação.");
        }
    }

    private void processLogLine(FileSystemSimulator fs, String line) {
        try {
            
            // Formato da linha: [2025-01-02 12:06:50] CREATE_DIR | /pasta1
            String[] parts = line.split(" \\| ");
            if (parts.length < 2) {
                System.out.println("Linha do journal mal formatada: " + line);
                return;
            }

            String actionPart = parts[0];
            String detail = parts[1].trim();

            int lastBracket = actionPart.lastIndexOf(']');
            if (lastBracket == -1) {
                System.out.println("Formato de timestamp inválido: " + line);
                return;
            }
            
            String action = actionPart.substring(lastBracket + 1).trim();

            System.out.println("Recuperando: " + action + " -> " + detail);

            switch (action) {
                case "CREATE_FILE":
                    String fileName = detail.substring(detail.lastIndexOf('/') + 1);
                    fs.makeFile(fileName);
                    break;
                case "DELETE_FILE":
                    String deleteFileName = detail.substring(detail.lastIndexOf('/') + 1);
                    fs.deleteFile(deleteFileName);
                    break;
                case "CREATE_DIR":
                    String dirName = detail.substring(detail.lastIndexOf('/') + 1);
                    fs.makeDirectory(dirName);
                    break;
                case "DELETE_DIR":
                    String deleteDirName = detail.substring(detail.lastIndexOf('/') + 1);
                    fs.deleteDirectory(deleteDirName);
                    break;
                case "RENAME_FILE": {
                    String[] renameParts = detail.split(" -> ");
                    if (renameParts.length == 2) {
                        String oldName = renameParts[0].substring(renameParts[0].lastIndexOf('/') + 1);
                        String newName = renameParts[1].substring(renameParts[1].lastIndexOf('/') + 1);
                        fs.renameFile(oldName, newName);
                    }
                    break;
                }
                case "RENAME_DIR": {
                    String[] renameParts = detail.split(" -> ");
                    if (renameParts.length == 2) {
                        String oldName = renameParts[0].substring(renameParts[0].lastIndexOf('/') + 1);
                        String newName = renameParts[1].substring(renameParts[1].lastIndexOf('/') + 1);
                        fs.renameDirectory(oldName, newName);
                    }
                    break;
                }
                case "WRITE_FILE": {
                    String[] writeParts = detail.split(" -> ");
                    if (writeParts.length == 2) {
                        String fileName2 = writeParts[0].substring(writeParts[0].lastIndexOf('/') + 1);
                        String content = writeParts[1];
                        fs.writeFile(fileName2, content);
                    }
                    break;
                }
                default:
                    System.out.println("Ação desconhecida no journal: " + action);
            }
        } catch (Exception e) {
            System.out.println("Erro ao processar linha do journal: " + line);
            System.out.println("ERRO: " + e.getMessage());
        }
    }
}
