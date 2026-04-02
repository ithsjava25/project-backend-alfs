package org.example.alfs.dto.attachment;

import java.time.LocalDateTime;

public class AttachmentViewDTO {
    private Long id;
    private Long ticketId;
    private String fileName;
    private String s3Key;
    private LocalDateTime uploadedAt;

    public AttachmentViewDTO() {}

    public AttachmentViewDTO(Long id, Long ticketId, String fileName, String s3Key, LocalDateTime uploadedAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.fileName = fileName;
        this.s3Key = s3Key;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
