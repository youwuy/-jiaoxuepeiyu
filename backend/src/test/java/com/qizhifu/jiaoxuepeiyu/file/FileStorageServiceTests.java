package com.qizhifu.jiaoxuepeiyu.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.file.model.StoredFile;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class FileStorageServiceTests {

    @TempDir
    Path tempDir;

    @Test
    void storesFileAndReturnsPublicMetadata() throws Exception {
        FileStorageService service = new FileStorageService(tempDir.toString(), "/uploads", 200L * 1024L * 1024L);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lesson.pdf",
                "application/pdf",
                "course-content".getBytes("UTF-8"));

        StoredFile stored = service.store(file, "resources");

        assertEquals("lesson.pdf", stored.getFileName());
        assertEquals(Long.valueOf(file.getSize()), stored.getFileSize());
        assertEquals("application/pdf", stored.getContentType());
        assertEquals("resources", stored.getCategory());
        assertTrue(stored.getStoredFileName().endsWith(".pdf"));
        assertTrue(stored.getFileUrl().startsWith("/uploads/resources/"));
        assertTrue(Files.exists(tempDir.resolve("resources").resolve(stored.getStoredFileName())));
    }

    @Test
    void stripsUnsafeOriginalFilePathFromStoredName() throws Exception {
        FileStorageService service = new FileStorageService(tempDir.toString(), "/uploads", 200L * 1024L * 1024L);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "..\\..\\secret.txt",
                "text/plain",
                "safe".getBytes("UTF-8"));

        StoredFile stored = service.store(file, "docs");

        assertFalse(stored.getStoredFileName().contains(".."));
        assertFalse(stored.getStoredFileName().contains("\\"));
        assertFalse(stored.getStoredFileName().contains("/"));
        assertTrue(stored.getStoredFileName().endsWith(".txt"));
        assertTrue(Files.exists(tempDir.resolve("docs").resolve(stored.getStoredFileName())));
    }

    @Test
    void rejectsUnsafeCategory() {
        FileStorageService service = new FileStorageService(tempDir.toString(), "/uploads", 200L * 1024L * 1024L);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lesson.pdf",
                "application/pdf",
                "course-content".getBytes());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.store(file, "../admin"));

        assertEquals("File category is invalid", exception.getMessage());
    }

    @Test
    void rejectsEmptyFile() {
        FileStorageService service = new FileStorageService(tempDir.toString(), "/uploads", 200L * 1024L * 1024L);
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.store(file, "resources"));

        assertEquals("File is required", exception.getMessage());
    }
}
