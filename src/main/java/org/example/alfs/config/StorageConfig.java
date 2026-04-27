package org.example.alfs.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class StorageConfig {

  @Bean
  public MinioClient minioClient(S3Properties props) {
    // Note: TLS is controlled by the scheme in props.getEndpoint() (http:// or https://)
    // The props.getSecure() flag should be reflected in the endpoint URL scheme
    return MinioClient.builder()
        .endpoint(props.getEndpoint())
        .credentials(props.getAccessKey(), props.getSecretKey())
        .region(props.getRegion())
        .build();
  }
}
