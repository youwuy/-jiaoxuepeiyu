package com.qizhifu.jiaoxuepeiyu.file.model;

public class StoredFile {

    private String fileUrl;
    private String fileName;
    private String storedFileName;
    private Long fileSize;
    private String contentType;
    private String category;

    public StoredFile() {
    }

    public StoredFile(String fileUrl,
                      String fileName,
                      String storedFileName,
                      Long fileSize,
                      String contentType,
                      String category) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.storedFileName = storedFileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.category = category;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
