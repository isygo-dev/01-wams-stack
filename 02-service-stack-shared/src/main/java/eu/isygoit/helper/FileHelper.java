package eu.isygoit.helper;

import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The type File helper.
 */
@Slf4j
public class FileHelper {

    /**
     * Make directory if not exist.
     *
     * @param dirPath the dir path
     */
    public static void makeDirectoryIfNotExist(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Store multipart file path.
     *
     * @param directory the directory
     * @param fileName  the file name
     * @param file      the file
     * @param extension the extension
     * @return the path
     * @throws IOException the io exception
     */
    public static Path storeMultipartFile(String directory, String fileName, MultipartFile file, String extension) throws IOException {
        if (file != null && !file.isEmpty()) {
            makeDirectoryIfNotExist(directory);

            Path fileNamePath = Paths.get(directory
                            + File.separator,
                    StringUtils.hasText(fileName)
                            ? fileName.concat(".").concat(StringUtils.hasText(extension) ? extension : FilenameUtils.getExtension(file.getOriginalFilename()))
                            : file.getOriginalFilename());
            Files.write(fileNamePath, file.getBytes());
            return fileNamePath;
        } else {
            log.warn(LogConstants.EMPTY_FILE_PROVIDED);
            throw new BadArgumentException(LogConstants.EMPTY_FILE_PROVIDED);
        }
    }

    /**
     * Gets properties.
     *
     * @param filename the filename
     * @return the properties
     */
    public static String getProperties(String filename) {
        InputStream input;
        try {
            input = new FileInputStream(filename);
            String theString = "";
            try {
                theString = IOUtils.toString(input, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return theString;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete dir boolean.
     *
     * @param dir   the dir
     * @param force the force
     * @return the boolean
     * @throws NotDirectoryException the not directory exception
     */
    public static boolean deleteDir(File dir, boolean force) throws NotDirectoryException {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]), force);
                if (!success)
                    return false;
            }
            return dir.delete();
        }

        throw new NotDirectoryException(dir.getName());
    }

    /**
     * Zip folder.
     *
     * @param srcFolder   the src folder
     * @param destZipFile the dest zip file
     * @throws Exception the exception
     */
    static public void zipFolder(String srcFolder, String destZipFile) throws Exception {
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;
        fileWriter = new FileOutputStream(destZipFile);
        zip = new ZipOutputStream(fileWriter);
        addFolderToZip("", srcFolder, zip);
        zip.flush();
        zip.close();
    }

    static private void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            try (FileInputStream in = new FileInputStream(srcFile)) {
                zip.putNextEntry(new ZipEntry(path
                        + File.separator + folder.getName()));
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
            } catch (FileNotFoundException e) {
                log.error("<Error>: {} ", e);
            } catch (IOException e) {
                log.error("<Error>: {} ", e);
            }
        }
    }

    /**
     * Add files to zip.
     *
     * @param path     the path
     * @param srcFiles the src files
     * @param zip      the zip
     * @throws Exception the exception
     */
    static public void addFilesToZip(String path, List<String> srcFiles, ZipOutputStream zip) throws Exception {
        try {
            srcFiles.forEach(srcFile -> {
                File folder = new File(srcFile);
                if (folder.isDirectory()) {
                    try {
                        addFolderToZip(path, srcFile, zip);
                    } catch (Exception e) {
                        log.error("<Error>: {} ", e);
                    }
                } else {
                    byte[] buf = new byte[1024];
                    int len;
                    try (FileInputStream in = new FileInputStream(srcFile)) {
                        zip.putNextEntry(new ZipEntry(path
                                + File.separator + folder.getName()));
                        while ((len = in.read(buf)) > 0) {
                            zip.write(buf, 0, len);
                        }
                    } catch (FileNotFoundException e) {
                        log.error("<Error>: {} ", e);
                    } catch (IOException e) {
                        log.error("<Error>: {} ", e);
                    }
                }
            });
        } catch (Throwable e) {
            log.error("<Error>: {} ", e);
        }
    }

    static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFolder);

        for (String fileName : folder.list()) {
            if (path.equals("")) {
                addFileToZip(folder.getName(), srcFolder
                        + File.separator + fileName, zip);
            } else {
                addFileToZip(path
                        + File.separator + folder.getName(), srcFolder + File.separator + fileName, zip);
            }
        }
    }

    /**
     * Build list path results list.
     *
     * @param fileCompleteRemotePath  the file complete remote path
     * @param fileCompleteRemotePath2 the file complete remote path 2
     * @return the list
     */
    public static List<String> buildListPathResults(String fileCompleteRemotePath, String fileCompleteRemotePath2) {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        String generatedPathResult = cal.get(Calendar.YEAR) + "_";
        DecimalFormat formatter = new DecimalFormat("#00.###");
        int month = cal.get(Calendar.MONTH) + 1;
        generatedPathResult += formatter.format(month);
        int today = cal.get(Calendar.DAY_OF_MONTH);
        int yesterday = today - 1;
        String validGeneratedFileForToday = fileCompleteRemotePath
                + File.separator + generatedPathResult
                + File.separator + formatter.format(today);
        String validGeneratedFileForYesterday = fileCompleteRemotePath
                + File.separator + generatedPathResult
                + File.separator + formatter.format(yesterday);
        String invalidGeneratedFileForToday = fileCompleteRemotePath2
                + File.separator + generatedPathResult
                + File.separator + formatter.format(today);
        String invalidGeneratedFileForYesterday = fileCompleteRemotePath2
                + File.separator + generatedPathResult
                + File.separator + formatter.format(yesterday);
        List<String> listPaths = new ArrayList<>();
        listPaths.add(invalidGeneratedFileForYesterday);
        listPaths.add(invalidGeneratedFileForToday);
        listPaths.add(validGeneratedFileForYesterday);
        listPaths.add(validGeneratedFileForToday);
        return listPaths;
    }

    /**
     * Add empty folder.
     *
     * @param srcFolder the src folder
     * @throws Exception the exception
     */
    public static void addEmptyFolder(String srcFolder) throws Exception {
        File folder = new File(srcFolder);
        folder.mkdir();
    }

    /**
     * Checksum mapped file long.
     *
     * @param filepath the filepath
     * @return the long
     * @throws IOException the io exception
     */
    public static Long checksumMappedFile(String filepath) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(filepath)) {
            FileChannel fileChannel = inputStream.getChannel();
            int len = (int) fileChannel.size();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, len);
            CRC32 crc = new CRC32();
            for (int cnt = 0; cnt < len; cnt++) {
                int i = buffer.get(cnt);
                crc.update(i);
            }

            return crc.getValue();
        }
    }
}
