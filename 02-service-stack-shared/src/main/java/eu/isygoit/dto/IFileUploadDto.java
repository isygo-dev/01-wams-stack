package eu.isygoit.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * The interface File upload dto.
 */
public interface IFileUploadDto extends IDto {

    /**
     * Gets file.
     *
     * @return the file
     */
    MultipartFile getFile();

    /**
     * Sets file.
     *
     * @param file the file
     */
    void setFile(MultipartFile file);

    /**
     * Gets original file name.
     *
     * @return the original file name
     */
    String getOriginalFileName();

    /**
     * Sets original file name.
     *
     * @param originalFileName the original file name
     */
    void setOriginalFileName(String originalFileName);
}
