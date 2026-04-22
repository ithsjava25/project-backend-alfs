package org.example.alfs.dto.attachment;

/**
 * Litet svar-DTO för att returnera en presignerad URL och dess TTL i sekunder.
 * Används av kommande presign-endpoint.
 */
public class PresignedUrlResponseDTO {
    private final String url;
    private final int expiresInSeconds;

    public PresignedUrlResponseDTO(String url, int expiresInSeconds) {
        this.url = url;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getUrl() {
        return url;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
