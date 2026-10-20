package com.pawlink.user;

import java.time.LocalDateTime;

public class UserDocumentDTO {
    private Long id;
    private Long userId;
    private String documentType;
    private String fileName;
    private String filePath;
    private long fileSize;
    private LocalDateTime uploadedAt;

    public UserDocumentDTO() {}

    public UserDocumentDTO(Long id, Long userId, String documentType, String fileName, String filePath, long fileSize, LocalDateTime uploadedAt) {
        this.id = id;
        this.userId = userId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}

