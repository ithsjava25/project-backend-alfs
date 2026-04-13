package org.example.alfs.dto.attachment;

import java.time.LocalDateTime;

public class AttachmentViewDTO {
    private Long id;
    private Long ticketId;
    private String fileName;
    private String downloadUrl;
    private LocalDateTime uploadedAt;

    public AttachmentViewDTO() {}

    public AttachmentViewDTO(Long id, Long ticketId, String fileName, String downloadUrl, LocalDateTime uploadedAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
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

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
