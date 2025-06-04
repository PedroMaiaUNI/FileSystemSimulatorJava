import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    public void log(String action) {
        log(action, "-");
    }

    public void close() {
        try {
            log("CHECKPOINT", "Programa salvo");
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
            List<String> logs = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                logs.add(line);
            }

            int startIndex = 0;
            for (int i = logs.size() - 1; i >= 0; i--) {
                if (logs.get(i).contains("CHECKPOINT")) {
                    startIndex = i + 1;
                    break;
                }
            }

            System.out.println("Iniciando recuperação via journal...");

            for (int i = startIndex; i < logs.size(); i++) {
                processLogLine(fs, logs.get(i));
            }

            System.out.println("Recuperação finalizada.");
        } catch (IOException e) {
            System.out.println("Nenhum journal encontrado para recuperação.");
        }
    }

    private void processLogLine(FileSystemSimulator fs, String line) {
        try {
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

            if (action.equals("CHECKPOINT")) return;

            System.out.println("Recuperando: " + action + " -> " + detail);

            switch (action) {
                case "CREATE_FILE":
                    fs.makeFile(detail);
                    break;
                case "DELETE_FILE":
                    fs.deleteFile(detail);
                    break;
                case "CREATE_DIR":
                    fs.makeDirectory(detail);
                    break;
                case "DELETE_DIR":
                    fs.deleteDirectory(detail);
                    break;
                case "RENAME_FILE": {
                    String[] renameParts = detail.split(" -> ");
                    if (renameParts.length == 2) {
                        fs.renameFile(renameParts[0].trim(), renameParts[1].trim());
                    }
                    break;
                }
                case "RENAME_DIR": {
                    String[] renameParts = detail.split(" -> ");
                    if (renameParts.length == 2) {
                        fs.renameDirectory(renameParts[0].trim(), renameParts[1].trim());
                    }
                    break;
                }
                case "WRITE_FILE": {
                    String[] writeParts = detail.split(" -> ");
                    if (writeParts.length == 2) {
                        String file = writeParts[0].trim();
                        String content = writeParts[1];
                        fs.writeFile(file, content);
                    }
                    break;
                }
                case "COPY_FILE":
                    fs.copyFile(detail);
                    break;
                case "PASTE_FILE":
                    fs.pasteFile();
                    break;
                case "CUT_FILE":
                    fs.cutFile(detail);
                    break;
                case "COPY_DIR":
                    fs.copyDirectory(detail);
                    break;
                case "PASTE_DIR":
                    fs.pasteDirectory();
                    break;
                case "CUT_DIR":
                    fs.cutDirectory(detail);
                    break;
                case "DUPLICATE_FILE":
                    fs.duplicateFile(detail);
                    break;
                case "DUPLICATE_DIR":
                    fs.duplicateDirectory(detail);
                    break;
                case "CHANGE_DIR":
                    fs.changeDirectory(detail);
                    break;
                default:
                    System.out.println("Ação desconhecida no journal: " + action);
            }
        } catch (Exception e) {
            System.out.println("Erro ao processar linha do journal: " + line);
            System.out.println("ERRO: " + e.getMessage());
        }
    }
}
