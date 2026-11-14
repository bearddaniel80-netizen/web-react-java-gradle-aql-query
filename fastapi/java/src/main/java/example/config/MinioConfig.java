package example.config;

import example.security.SecretReader;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(
            SecretReader secrets,
            @Value("${minio.endpoint}") String endpoint) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(
                        secrets.read("minio_app_user"),
                        secrets.read("minio_app_password"))
                .build();
    }
}
