package com.qizhifu.jiaoxuepeiyu.file;

import com.qizhifu.jiaoxuepeiyu.file.model.StoredFile;
import com.qizhifu.jiaoxuepeiyu.file.repository.StoredFileMapper;
import org.springframework.stereotype.Service;

@Service
public class StoredFileRegistry {

    private final StoredFileMapper mapper;

    public StoredFileRegistry(StoredFileMapper mapper) {
        this.mapper = mapper;
    }

    public StoredFile register(StoredFile file, Long uploaderId) {
        mapper.insert(file, uploaderId);
        return file;
    }
}
