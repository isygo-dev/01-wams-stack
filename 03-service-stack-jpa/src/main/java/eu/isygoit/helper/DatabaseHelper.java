package eu.isygoit.helper;

import eu.isygoit.exception.BackupCommandException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

/**
 * The type Database helper with additional features.
 */
@Slf4j
public class DatabaseHelper {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;  // 2 seconds retry delay

    /**
     * Execute command with retry logic.
     *
     * @param datasourceUrl    the datasource url
     * @param databaseName     the database name
     * @param databaseUser     the database user
     * @param databasePassword the database password
     * @param dumpDir          the dump dir
     * @param backupFileName   the backup file name
     * @param bcpType          the backup operation type
     */
    public static void executeCommandWithRetry(String datasourceUrl, String databaseName, String databaseUser, String databasePassword, String dumpDir, String backupFileName, BackupOperation bcpType) {
        int attempts = 0;
        boolean success = false;

        while (attempts < MAX_RETRIES && !success) {
            try {
                executeCommand(datasourceUrl, databaseName, databaseUser, databasePassword, dumpDir, backupFileName, bcpType);
                success = true;  // If successful, exit the loop
            } catch (BackupCommandException e) {
                attempts++;
                log.error("Attempt {} failed. Retrying in {}ms", attempts, RETRY_DELAY_MS);
                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log.error("All attempts failed. Aborting operation.");
                }
            }
        }
    }

    /**
     * Execute the command.
     *
     * @param datasourceUrl    the datasource url
     * @param databaseName     the database name
     * @param databaseUser     the database user
     * @param databasePassword the database password
     * @param dumpDir          the dump dir
     * @param backupFileName   the backup file name
     * @param bcpType          the backup operation type
     */
    public static void executeCommand(String datasourceUrl, String databaseName, String databaseUser, String databasePassword, String dumpDir, String backupFileName, BackupOperation bcpType) {
        List<String> command = buildPgCommands(datasourceUrl, databaseName, databaseUser, dumpDir, backupFileName, bcpType);
        if (!command.isEmpty()) {
            try {
                processCommand(databasePassword, command);
                log.info("Successfully executed command {}/{}", bcpType, String.join(" ", command));
            } catch (IOException e) {
                log.error("Failed to execute command {}/{} with exception", bcpType, String.join(" ", command), e);
                throw new BackupCommandException(e);
            } catch (InterruptedException e) {
                log.error("Command interrupted {}/{} with exception", bcpType, String.join(" ", command), e);
                Thread.currentThread().interrupt();
            }
        } else {
            log.warn("Error: Failed to build PgCommands. Invalid parameters.");
            throw new BackupCommandException("Error: Failed to build PgCommands. Invalid parameters.");
        }
    }

    private static void processCommand(String databasePassword, List<String> commands) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.environment().put("PGPASSWORD", databasePassword);

        Process process = pb.start();

        // Capture standard output for logging
        try (BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            buf.lines().forEach(log::info);  // Log process output for visibility
        }

        // Capture standard error stream for logging
        try (BufferedReader buf = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            buf.lines().forEach(log::warn);  // Log error stream lines
        }

        int exitCode = process.waitFor();
        process.destroy();
        if (exitCode != 0) {
            throw new IOException("Process terminated with non-zero exit code: " + exitCode);
        }
    }

    /**
     * Build pg commands list.
     *
     * @param datasourceUrl  the datasource url
     * @param databaseName   the database name
     * @param databaseUser   the database user
     * @param dumpDir        the dump dir
     * @param backupFileName the backup file name
     * @param bcpType        the backup operation type
     * @return the list
     */
    public static List<String> buildPgCommands(String datasourceUrl, String databaseName, String databaseUser, String dumpDir, String backupFileName, BackupOperation bcpType) {
        File backupFilePath = new File(dumpDir);
        List<String> command = null;
        switch (bcpType) {
            case DUMP:
                if (!backupFilePath.exists()) {
                    backupFilePath.mkdirs();
                }
                command = List.of(
                        "pg_dump",
                        "-h", datasourceUrl.split(":")[0],
                        "-p", datasourceUrl.split(":")[1],
                        "-U", databaseUser,
                        "-F", "t",  // t = .tra
                        "-b", "-v", "-f", Path.of(backupFilePath.getAbsolutePath()).resolve(backupFileName).toString(),
                        "-d", databaseName,
                        "--column-inserts", "--attribute-inserts"
                );
                break;
            case LOAD:
                if (!backupFilePath.exists()) {
                    return List.of();  // Return empty list if no file exists
                }
                command = List.of(
                        "pg_restore",
                        "-h", datasourceUrl.split(":")[0],
                        "-p", datasourceUrl.split(":")[1],
                        "--clean",
                        "-F", "t",
                        "-U", databaseUser,
                        "-d", databaseName,
                        "-v", Path.of(backupFilePath.getAbsolutePath()).resolve(backupFileName).toString()
                );
                break;
            default:
                log.error("Unsupported backup type: [{}]", bcpType);
                break;
        }
        return command != null ? command : List.of();
    }

    /**
     * Verify if a backup file exists before restoring.
     *
     * @param dumpDir        the dump dir
     * @param backupFileName the backup file name
     * @return true if file exists, false otherwise
     */
    public static boolean verifyBackupFileExistence(String dumpDir, String backupFileName) {
        File file = new File(Path.of(dumpDir).resolve(backupFileName).toString());
        return file.exists();
    }

    /**
     * The enum Backup operation.
     */
    public enum BackupOperation {
        /**
         * Load backup operation.
         */
        LOAD,  // Load backup operation
        /**
         * The Dump.
         */
        DUMP   // Dump backup operation
    }
}
