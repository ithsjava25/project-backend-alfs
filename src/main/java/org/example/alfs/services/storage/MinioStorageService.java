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
import java.time.Duration;
import java.util.UUID;

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
        String objectKey = UUID.randomUUID() + "/" + sanitize(fileName);

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
     * Skapar en tidsbegränsad försignerad GET-URL för ett objekt.
     * Ingen åtkomstkontroll här – detta är endast en låg-nivå hjälpfunktion.
     * Åtkomstkontrollen ska ske innan denna metod anropas.
     */
    public String createPresignedGetUrl(String objectKey, Duration expiry) throws Exception {
        int seconds = clampExpirySeconds(expiry);
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .bucket(props.getBucket())
                .object(objectKey)
                .method(Method.GET)
                .expiry(seconds)
                .build();
        return minioClient.getPresignedObjectUrl(args);
    }

    private int clampExpirySeconds(Duration d) {
        // MinIO (S3) brukar begränsa presigned URL-expiry till max 7 dagar.
        // Vi håller oss mellan 60 sek och 7 dagar (604800 sek) som rimliga defaults.
        if (d == null) return 15 * 60; // 15 min
        long s = d.toSeconds();
        if (s < 60) s = 60;
        if (s > 604800) s = 604800;
        return (int) s;
    }

    private String sanitize(String name) {
        return name.replace("\\", "_").replace("/", "_");
    }
}
