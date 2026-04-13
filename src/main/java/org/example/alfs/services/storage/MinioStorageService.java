package org.example.alfs.services.storage;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.example.alfs.config.S3Properties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
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

    private String sanitize(String name) {
        return name.replace("\\", "_").replace("/", "_");
    }
}
