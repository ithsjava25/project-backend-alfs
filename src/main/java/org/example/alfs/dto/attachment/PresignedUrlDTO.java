package org.example.alfs.dto.attachment;

public class PresignedUrlDTO {
    private String presignedUrl;

    public PresignedUrlDTO() {
    }

    public PresignedUrlDTO(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }
}
