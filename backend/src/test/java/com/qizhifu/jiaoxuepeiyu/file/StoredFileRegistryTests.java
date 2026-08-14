package com.qizhifu.jiaoxuepeiyu.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.qizhifu.jiaoxuepeiyu.file.model.StoredFile;
import com.qizhifu.jiaoxuepeiyu.file.repository.StoredFileMapper;
import org.junit.jupiter.api.Test;

class StoredFileRegistryTests {

    @Test
    void registersUploadedFileAndReturnsGeneratedId() {
        RecordingMapper mapper = new RecordingMapper();
        StoredFileRegistry registry = new StoredFileRegistry(mapper);
        StoredFile file = new StoredFile(
                "/uploads/user-faces/face.png",
                "face.png",
                "stored-face.png",
                Long.valueOf(128L),
                "image/png",
                "user-faces");

        StoredFile result = registry.register(file, Long.valueOf(42L));

        assertSame(file, result);
        assertEquals(Long.valueOf(1001L), result.getFileId());
        assertEquals(Long.valueOf(42L), mapper.uploaderId);
    }

    private static class RecordingMapper implements StoredFileMapper {
        private Long uploaderId;

        @Override
        public void insert(StoredFile file, Long uploaderId) {
            this.uploaderId = uploaderId;
            file.setFileId(Long.valueOf(1001L));
        }
    }
}
