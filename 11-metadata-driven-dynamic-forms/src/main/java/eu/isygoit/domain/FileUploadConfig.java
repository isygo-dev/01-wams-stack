package eu.isygoit.domain;

public record FileUploadConfig(
        boolean multiple,
        String[] acceptedTypes,
        long maxFileSize,
        String uploadUrl
) {}