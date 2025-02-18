package eu.isygoit.helper;

import eu.isygoit.exception.BackupCommandException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Database helper.
 */
@Slf4j
public class DatabaseHelper {

    /**
     * Execute command.
     *
     * @param datasourceUrl    the datasource url
     * @param databaseName     the database name
     * @param databaseUser     the database user
     * @param databasePassword the database password
     * @param dumpDir          the dump dir
     * @param backupFileName   the backup file name
     * @param bcpType          the bcp type
     */
    public static synchronized void executeCommand(String datasourceUrl, String databaseName, String databaseUser, String databasePassword, String dumpDir, String backupFileName, BackupOperation bcpType) {
        List<String> command = buildPgComands(datasourceUrl, databaseName, databaseUser, dumpDir, backupFileName, bcpType);
        if (!CollectionUtils.isEmpty(command)) {
            try {
                processCommand(databasePassword, command);
                log.info("Successful command {}/{}", bcpType, String.join(" ", command));
            } catch (IOException e) {
                log.info("Faild command {}/{} with exception", bcpType, String.join(" ", command), e);
                throw new BackupCommandException(e);
            } catch (InterruptedException e) {
                log.info("Faild command {}/{} with exception", bcpType, String.join(" ", command), e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        } else {
            log.warn("Error: build Pg Comands failed, Invalid params.");
            throw new BackupCommandException("Error: build Pg Comands failed, Invalid params");
        }
    }

    public static synchronized void processCommand(String databasePassword, List<String> commands) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.environment().put("PGPASSWORD", databasePassword);

        Process process = pb.start();

        try (BufferedReader buf = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line = buf.readLine();
            while (line != null) {
                line = buf.readLine();
            }
        }
        process.waitFor();
        process.destroy();
    }

    /**
     * Build pg comands list.
     *
     * @param datasourceUrl  the datasource url
     * @param databaseName   the database name
     * @param databaseUser   the database user
     * @param dumpDir        the dump dir
     * @param backupFileName the backup file name
     * @param bcpType        the bcp type
     * @return the list
     */
    public static List<String> buildPgComands(String datasourceUrl, String databaseName, String databaseUser, String dumpDir, String backupFileName, BackupOperation bcpType) {
        File backupFilePath = new File(dumpDir);
        ArrayList<String> command = null;
        switch (bcpType) {
            case DUMP:
                if (!backupFilePath.exists()) {
                    File dir = backupFilePath;
                    dir.mkdirs();
                }
                command = new ArrayList<>();
                command.add("pg_dump");
                command.add("-h"); //database server host
                command.add(datasourceUrl.split(":")[0]);
                command.add("-p"); //database server port number
                command.add(datasourceUrl.split(":")[1]);
                command.add("-U"); //connect as specified database user
                command.add(databaseUser);
                command.add("-F"); //output file format (custom, directory, tar, plain text (default))
                command.add("t"); // t = .tra
                command.add("-b"); //include large objects in dump
                command.add("-v"); //verbose mode
                command.add("-f"); //output file or directory name
                command.add(backupFilePath.getAbsolutePath()
                        + File.separator + backupFileName);
                command.add("-d"); //database name
                command.add(databaseName);
                command.add("--column-inserts");
                command.add("--attribute-inserts");
                break;
            case LOAD:
                if (!backupFilePath.exists()) {
                    return command;
                }
                command = new ArrayList<>();
                command.add("pg_restore");
                command.add("-h");
                command.add(datasourceUrl.split(":")[0]);
                command.add("-p");
                command.add(datasourceUrl.split(":")[1]);
                command.add("--clean");
                command.add("-F");
                command.add("t");
                command.add("-U");
                command.add(databaseUser);
                command.add("-d");
                command.add(databaseName);
                command.add("-v");
                command.add(backupFilePath.getAbsolutePath()
                        + File.separator + backupFileName);
                break;
            default:
                log.error("<Error>: Buckup type [{}] not supported", bcpType.name());
                break;
        }
        return command;
    }

    /**
     * The enum Backup operation.
     */
    public enum BackupOperation {
        /**
         * Load backup operation.
         */
        LOAD,
        /**
         * Dump backup operation.
         */
        DUMP
    }
}
