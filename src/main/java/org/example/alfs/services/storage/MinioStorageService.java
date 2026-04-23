package org.example.alfs.services.storage;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.example.alfs.config.S3Properties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;
    private final S3Properties props;

    public MinioStorageService(MinioClient minioClient, S3Properties props) {
        this.minioClient = minioClient;
        this.props = props;
    }

    /**
     * Laddar upp en fil till S3/MinIO och returnerar dess objectKey.
     */
    public String upload(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            fileName = "file";
        }
        String objectKey = UUID.randomUUID() + "/" + sanitizeFileName(fileName);

        try (InputStream is = file.getInputStream()) {
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .contentType(contentType)
                    .stream(is, file.getSize(), -1)
                    .build();
            minioClient.putObject(args);
        }

        return objectKey;
    }

    public GetObjectResponse download(String objectKey) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(props.getBucket())
                        .object(objectKey)
                        .build()
        );
    }

    public void delete(String objectKey) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(props.getBucket())
                        .object(objectKey)
                        .build()
        );
    }

    /**
     * Skapar en tidsbegränsad (pre-signerad) GET-URL för ett objekt i S3/MinIO.
     *
     * @param objectKey  S3/MinIO-objektets key
     * @param ttlSeconds Giltighetstid i sekunder (MinIO/S3 begränsar maxvärden, t.ex. upp till 7 dagar)
     * @return Publik URL som kan användas för direkt nedladdning under giltighetstiden
     */
    public String generatePresignedGetUrl(String objectKey, int ttlSeconds) throws Exception {
        if (ttlSeconds <= 0) {
            ttlSeconds = 60; // defensivt standardvärde
        }
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(props.getBucket())
                .object(objectKey)
                .expiry(ttlSeconds)
                .build();
        return minioClient.getPresignedObjectUrl(args);
    }

    /**
     * Variant som inkluderar response-content-disposition så att webbläsaren föreslår originalfilnamn.
     */
    public String generatePresignedGetUrlWithContentDisposition(String objectKey, int ttlSeconds, String fileName) throws Exception {
        if (ttlSeconds <= 0) {
            ttlSeconds = 60;
        }
        // Sanitize the supplied filename for safe use in headers and URLs
        String safeName = sanitizeFileName(fileName);
        String quoted = escapeForQuotedString(safeName);
        String rfc5987 = rfc5987Encode(safeName);
        // S3-kompatibla tjänster accepterar "response-content-disposition" som query-param
        String disposition = "attachment; filename=\"" + quoted + "\"; filename*=UTF-8''" + rfc5987;
        Map<String, String> extra = new HashMap<>();
        extra.put("response-content-disposition", disposition);

        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(props.getBucket())
                .object(objectKey)
                .expiry(ttlSeconds)
                .extraQueryParams(extra)
                .build();
        return minioClient.getPresignedObjectUrl(args);
    }

    /**
     * Sanitizes a file name for safe use in headers/paths:
     * - Trim whitespace
     * - Remove CR/LF and other control characters
     * - Replace path separators and dangerous characters with underscore
     * - Fallback to "file" if empty after sanitization
     */
    private String sanitizeFileName(String name) {
        if (name == null) return "file";
        String trimmed = name.trim();
        // remove control chars including CR(\r), LF(\n), tab, etc.
        StringBuilder sb = new StringBuilder(trimmed.length());
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c < 0x20 || c == 0x7F) {
                continue; // skip control chars
            }
            // Disallow path separators and reserved header-breaking chars
            if (c == '/' || c == '\\' || c == '"' || c == '\'' || c == ':' || c == ';' || c == ',' || c == '\n' || c == '\r') {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }
        String result = sb.toString();
        if (result.isBlank()) return "file";
        return result;
    }

    /**
     * Escapes a value for use inside a quoted-string (RFC 2616/7230 style):
     * backslash-escape backslashes and double quotes.
     */
    private String escapeForQuotedString(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * RFC 5987 percent-encoding for HTTP header parameters using UTF-8.
     * Spaces are encoded as %20 (not '+'), and non-ASCII bytes are percent-encoded.
     */
    private String rfc5987Encode(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            int v = b & 0xFF;
            // unreserved according to RFC 3986: ALPHA / DIGIT / "-" / "." / "_" / "~"
            if ((v >= 'a' && v <= 'z') || (v >= 'A' && v <= 'Z') || (v >= '0' && v <= '9')
                    || v == '-' || v == '.' || v == '_' || v == '~') {
                sb.append((char) v);
            } else if (v == ' ') {
                sb.append("%20");
            } else {
                sb.append('%');
                String hex = Integer.toHexString(v).toUpperCase();
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
        }
        return sb.toString();
    }
}
