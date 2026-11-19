package eu.isygoit.com.rest.service.media;

import eu.isygoit.dto.common.ResourceDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;

public final class FileDownloadResponses {

    private FileDownloadResponses() {}

    // ---------- DTO-based entry point (unchanged) ----------

    public static ResponseEntity<Resource> build(ResourceDto resourceDto) {
        if (resourceDto == null || resourceDto.getResource() == null) {
            return ResponseEntity.notFound().build();
        }
        String contentType = FileDownloadResponses.resolveContentType(resourceDto);
        String fileName = StringUtils.defaultIfBlank(
                resourceDto.getOriginalFileName(),
                resourceDto.getFileName());
        return build(resourceDto.getResource(), contentType, fileName);
    }

    // ---------- NEW: Resource-based entry point ----------

    /**
     * Builds a download response from a raw {@link Resource}.
     * <p>
     * The {@code Resource} may be an in-memory byte array (Feign), so this method never
     * calls {@code getFile()} or {@code Files.probeContentType}. The MIME type is resolved
     * from the resource's filename via {@link MediaTypeFactory}, then falls back to
     * {@code application/octet-stream}.
     *
     * @param resource         the resource to stream (must not be {@code null})
     * @param fallbackFileName filename to use when the resource exposes none
     *                         (e.g. {@code ByteArrayResource} without an explicit name)
     */
    public static ResponseEntity<Resource> build(Resource resource, String fallbackFileName) {
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        String fileName = StringUtils.defaultIfBlank(resource.getFilename(), fallbackFileName);
        String contentType = FileDownloadResponses.resolveContentType(resource, fileName);
        return build(resource, contentType, fileName);
    }

    // ---------- Shared assembly ----------

    private static ResponseEntity<Resource> build(Resource resource,
                                                  String contentType,
                                                  String fileName) {
        if (StringUtils.isBlank(fileName)) {
            fileName = "download";
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    // ---------- MIME resolution ----------

    /** Same logic as before, for {@link ResourceDto}. */
    public static String resolveContentType(ResourceDto dto) {
        String declared = dto.getFileType();
        if (StringUtils.isNotBlank(declared) && declared.contains("/")) {
            return declared;
        }
        String fileName = StringUtils.defaultIfBlank(
                dto.getOriginalFileName(),
                dto.getFileName());
        if (StringUtils.isNotBlank(fileName)) {
            MediaType fromName = MediaTypeFactory.getMediaType(fileName).orElse(null);
            if (fromName != null) {
                return fromName.toString();
            }
        }
        if (StringUtils.isNotBlank(declared)) {
            MediaType fromExt = MediaTypeFactory
                    .getMediaType("file." + declared.trim().toLowerCase())
                    .orElse(null);
            if (fromExt != null) {
                return fromExt.toString();
            }
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    /**
     * Resolves the MIME type of a raw {@link Resource}.
     * <ul>
     *   <li>First tries {@link MediaTypeFactory#getMediaType(Resource)}, which reads only
     *       {@code resource.getFilename()} — no filesystem access, safe for in-memory
     *       resources.</li>
     *   <li>Then falls back to the supplied {@code fileName} (useful when the resource
     *       itself has no name, e.g. a {@code ByteArrayResource} built from Feign bytes).</li>
     *   <li>Finally defaults to {@code application/octet-stream}.</li>
     * </ul>
     */
    public static String resolveContentType(Resource resource, String fileName) {
        if (resource != null) {
            MediaType fromResource = MediaTypeFactory.getMediaType(resource).orElse(null);
            if (fromResource != null) {
                return fromResource.toString();
            }
        }
        if (StringUtils.isNotBlank(fileName)) {
            MediaType fromName = MediaTypeFactory.getMediaType(fileName).orElse(null);
            if (fromName != null) {
                return fromName.toString();
            }
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}