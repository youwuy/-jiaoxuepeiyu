package com.qizhifu.jiaoxuepeiyu.file;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.file.model.StoredFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final String DEFAULT_CATEGORY = "general";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final Path uploadRoot;
    private final String publicPrefix;
    private final long maxSizeBytes;

    public FileStorageService(@Value("${app.file.upload-root:uploads}") String uploadRoot,
                              @Value("${app.file.public-prefix:/uploads}") String publicPrefix,
                              @Value("${app.file.max-size-bytes:209715200}") long maxSizeBytes) {
        this.uploadRoot = Paths.get(uploadRoot).toAbsolutePath().normalize();
        this.publicPrefix = normalizePublicPrefix(publicPrefix);
        this.maxSizeBytes = maxSizeBytes;
    }

    public StoredFile store(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "File is required");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessException(400, "File size exceeds limit");
        }

        String normalizedCategory = normalizeCategory(category);
        String originalFileName = cleanOriginalFileName(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID().toString().replace("-", "") + extensionOf(originalFileName);
        Path targetDirectory = uploadRoot.resolve(normalizedCategory).normalize();
        if (!targetDirectory.startsWith(uploadRoot)) {
            throw new BusinessException(400, "File category is invalid");
        }

        try {
            Files.createDirectories(targetDirectory);
            Path target = targetDirectory.resolve(storedFileName).normalize();
            if (!target.startsWith(targetDirectory)) {
                throw new BusinessException(400, "File name is invalid");
            }
            InputStream inputStream = file.getInputStream();
            try {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                inputStream.close();
            }
        } catch (IOException exception) {
            throw new BusinessException(500, "Failed to store file");
        }

        String fileUrl = publicPrefix + "/" + normalizedCategory + "/" + storedFileName;
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : DEFAULT_CONTENT_TYPE;
        return new StoredFile(fileUrl, originalFileName, storedFileName, Long.valueOf(file.getSize()), contentType, normalizedCategory);
    }

    private String normalizeCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return DEFAULT_CATEGORY;
        }
        String normalized = category.trim().toLowerCase(Locale.ENGLISH);
        if (!normalized.matches("[a-z0-9_-]{1,40}")) {
            throw new BusinessException(400, "File category is invalid");
        }
        return normalized;
    }

    private String cleanOriginalFileName(String originalFileName) {
        String normalized = StringUtils.hasText(originalFileName) ? originalFileName.replace("\\", "/") : "file";
        String filename = StringUtils.getFilename(normalized);
        if (!StringUtils.hasText(filename) || ".".equals(filename) || "..".equals(filename)) {
            return "file";
        }
        return filename;
    }

    private String extensionOf(String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);
        if (!StringUtils.hasText(extension)) {
            return "";
        }
        String normalized = extension.toLowerCase(Locale.ENGLISH);
        if (!normalized.matches("[a-z0-9]{1,16}")) {
            return "";
        }
        return "." + normalized;
    }

    private String normalizePublicPrefix(String publicPrefix) {
        String normalized = StringUtils.hasText(publicPrefix) ? publicPrefix.trim() : "/uploads";
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
