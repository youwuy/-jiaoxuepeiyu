package com.qizhifu.jiaoxuepeiyu.file.repository;

import com.qizhifu.jiaoxuepeiyu.file.model.StoredFile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StoredFileMapper {

    @Insert("INSERT INTO sys_uploaded_file "
            + "(file_url, file_name, stored_file_name, file_size, content_type, category, uploader_id, created_at) "
            + "VALUES (#{file.fileUrl}, #{file.fileName}, #{file.storedFileName}, #{file.fileSize}, "
            + "#{file.contentType}, #{file.category}, #{uploaderId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "file.fileId")
    void insert(@Param("file") StoredFile file, @Param("uploaderId") Long uploaderId);
}
