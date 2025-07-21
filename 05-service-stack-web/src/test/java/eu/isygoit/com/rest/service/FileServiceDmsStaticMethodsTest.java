package eu.isygoit.com.rest.service;

import eu.isygoit.com.rest.api.ILinkedFileApi;
import eu.isygoit.dto.common.LinkedFileRequestDto;
import eu.isygoit.dto.common.LinkedFileResponseDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.EntityNullException;
import eu.isygoit.exception.LinkedFileServiceNullException;
import eu.isygoit.exception.MultiPartFileNullException;
import eu.isygoit.model.Resume;
import eu.isygoit.model.ResumeLinkedFile;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * The type File api dms static methods test.
 */
class FileServiceDmsStaticMethodsTest {

    /**
     * Helper to create a Resume entity with given path, code, and filename.
     */
    private Resume createResume(String path, String code, String fileName) {
        return Resume.builder()
                .path(path)
                .code(code)
                .fileName(fileName)
                .tenant("testTenant")
                .build();
    }

    /**
     * Helper to create ResumeLinkedFile entity with path, code, and filename.
     */
    private ResumeLinkedFile createLinkedFile(String path, String code, String fileName) {
        return ResumeLinkedFile.builder()
                .tenant("testTenant")
                .path(path)
                .code(code)
                .fileName(fileName)
                .build();
    }

    /**
     * Upload should throw linked file api null exception when linked file api is null.
     *
     * @throws IOException the io exception
     */
    @Test
    void upload_shouldThrowLinkedFileServiceNullException_whenLinkedFileServiceIsNull() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        Resume entity = createResume("resume", "code123", "file.txt");

        LinkedFileServiceNullException ex = assertThrows(LinkedFileServiceNullException.class,
                () -> FileServiceDmsStaticMethods.upload(file, entity, null));
        assertEquals("LinkedFileApi api is null", ex.getMessage());
    }

    /**
     * Upload should throw multi part file null exception when multipart file is null.
     *
     * @throws IOException the io exception
     */
    @Test
    void upload_shouldThrowMultiPartFileNullException_whenMultipartFileIsNull() throws IOException {
        ILinkedFileApi linkedFileService = mock(ILinkedFileApi.class);
        Resume entity = createResume("resume", "code123", "file.txt");

        MultiPartFileNullException ex = assertThrows(MultiPartFileNullException.class,
                () -> FileServiceDmsStaticMethods.upload(null, entity, linkedFileService));
        assertEquals("MultipartFile must not be null", ex.getMessage());
    }

    /**
     * Upload should throw entity null exception when entity is null.
     *
     * @throws IOException the io exception
     */
    @Test
    void upload_shouldThrowEntityNullException_whenEntityIsNull() throws IOException {
        ILinkedFileApi linkedFileService = mock(ILinkedFileApi.class);
        MultipartFile file = mock(MultipartFile.class);

        EntityNullException ex = assertThrows(EntityNullException.class,
                () -> FileServiceDmsStaticMethods.upload(file, null, linkedFileService));
        assertEquals("Entity must not be null", ex.getMessage());
    }

    /**
     * Upload should return response when upload is successful.
     *
     * @throws IOException the io exception
     */
    @Test
    void upload_shouldReturnResponse_whenUploadIsSuccessful() throws IOException {
        ILinkedFileApi linkedFileService = mock(ILinkedFileApi.class);
        MultipartFile file = mock(MultipartFile.class);
        Resume entity = createResume("resume", "code123", "file.txt");
        LinkedFileResponseDto responseDto = mock(LinkedFileResponseDto.class);
        ResponseEntity<LinkedFileResponseDto> responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);

        // Use consistent matchers for all arguments
        when(linkedFileService.upload(any(RequestContextDto.class), any(LinkedFileRequestDto.class))).thenReturn(responseEntity);
        when(file.getOriginalFilename()).thenReturn("file.txt");

        LinkedFileResponseDto result = FileServiceDmsStaticMethods.upload(file, entity, linkedFileService);

        assertNotNull(result);
        verify(linkedFileService, times(1)).upload(any(RequestContextDto.class), any(LinkedFileRequestDto.class));
    }

    /**
     * Download should throw linked file api null exception when linked file api is null.
     *
     * @throws IOException the io exception
     */
    @Test
    void download_shouldThrowLinkedFileServiceNullException_whenLinkedFileServiceIsNull() throws IOException {
        Resume entity = createResume("resume", "code123", "file.txt");

        LinkedFileServiceNullException ex = assertThrows(LinkedFileServiceNullException.class,
                () -> FileServiceDmsStaticMethods.download(entity, 1L, null));
        assertEquals("LinkedFileApi api is null", ex.getMessage());
    }

    /**
     * Download should throw entity null exception when entity is null.
     *
     * @throws IOException the io exception
     */
    @Test
    void download_shouldThrowEntityNullException_whenEntityIsNull() throws IOException {
        ILinkedFileApi linkedFileService = mock(ILinkedFileApi.class);

        EntityNullException ex = assertThrows(EntityNullException.class,
                () -> FileServiceDmsStaticMethods.download(null, 1L, linkedFileService));
        assertEquals("Entity must not be null", ex.getMessage());
    }

    /**
     * Download should return resource when download is successful.
     *
     * @throws IOException the io exception
     */
    @Test
    void download_shouldReturnResource_whenDownloadIsSuccessful() throws IOException {
        ILinkedFileApi linkedFileService = mock(ILinkedFileApi.class);
        Resume entity = createResume("resume", "code123", "file.txt");
        Resource resource = mock(Resource.class);
        ResponseEntity<Resource> responseEntity = new ResponseEntity<>(resource, HttpStatus.OK);

        when(linkedFileService.download(any(), anyString(), anyString())).thenReturn(responseEntity);

        Resource result = FileServiceDmsStaticMethods.download(entity, 1L, linkedFileService);

        assertNotNull(result);
        verify(linkedFileService, times(1)).download(any(), anyString(), anyString());
    }

    /**
     * Delete should throw linked file api null exception when linked file api is null.
     */
    @Test
    void delete_shouldThrowLinkedFileServiceNullException_whenLinkedFileServiceIsNull() {
        ResumeLinkedFile entity = createLinkedFile("resumeLinkedFile", "code123", "file.txt");

        LinkedFileServiceNullException ex = assertThrows(LinkedFileServiceNullException.class,
                () -> FileServiceDmsStaticMethods.delete(entity, null));
        assertEquals("LinkedFileApi api is null", ex.getMessage());
    }

    /**
     * Delete should throw entity null exception when entity is null.
     */
    @Test
    void delete_shouldThrowEntityNullException_whenEntityIsNull() {
        ILinkedFileApi linkedFileService = mock(ILinkedFileApi.class);

        EntityNullException ex = assertThrows(EntityNullException.class,
                () -> FileServiceDmsStaticMethods.delete(null, linkedFileService));
        assertEquals("Entity must not be null", ex.getMessage());
    }

    /**
     * Delete should return true when deletion is successful.
     */
    @Test
    void delete_shouldReturnTrue_whenDeletionIsSuccessful() {
        ILinkedFileApi linkedFileService = mock(ILinkedFileApi.class);
        ResumeLinkedFile entity = createLinkedFile("resumeLinkedFile", "code123", "file.txt");
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);

        when(linkedFileService.deleteFile(any(), anyString(), anyString())).thenReturn(responseEntity);

        boolean result = FileServiceDmsStaticMethods.delete(entity, linkedFileService);

        assertTrue(result);
        verify(linkedFileService, times(1)).deleteFile(any(), anyString(), anyString());
    }

    /**
     * Delete should return false when deletion fails.
     */
    @Test
    void delete_shouldReturnFalse_whenDeletionFails() {
        ILinkedFileApi linkedFileService = mock(ILinkedFileApi.class);
        ResumeLinkedFile entity = createLinkedFile("resumeLinkedFile", "code123", "file.txt");
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(Boolean.FALSE, HttpStatus.OK);

        when(linkedFileService.deleteFile(any(), anyString(), anyString())).thenReturn(responseEntity);

        boolean result = FileServiceDmsStaticMethods.delete(entity, linkedFileService);

        assertFalse(result);
    }
}