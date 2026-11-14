package example.storage;

import example.model.UploadUrlRequest;
import example.model.UploadUrlResponse;
import example.security.InternalKeyVerifier;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UploadController {

    private final MinioClient minio;
    private final InternalKeyVerifier keys;
    private final String bucket;

    public UploadController(
            MinioClient minio,
            InternalKeyVerifier keys,
            @Value("${minio.bucket}") String bucket) {
        this.minio = minio;
        this.keys = keys;
        this.bucket = bucket;
    }

    // Generate presigned upload URL
    @PostMapping("/api/uploads")
    public UploadUrlResponse createUploadUrl(
            @RequestBody UploadUrlRequest file,
            @RequestHeader(value = InternalKeyVerifier.HEADER, required = false) String key) {
        keys.verify(key);

        // e.g. 550e8400-e29b-41d4-a716-446655440000.pdf
        String filename = file.getFilename() == null ? "" : file.getFilename();
        String extension = filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.'))
                : "";
        String objectName = UUID.randomUUID() + extension;

        return new UploadUrlResponse(objectName, presign(Method.PUT, objectName, 1, TimeUnit.DAYS));
    }

    // Generate download URL (not key-protected, same as the FastAPI original)
    @GetMapping("/api/downloads/{*objectName}")
    public Map<String, String> createDownloadUrl(@PathVariable String objectName) {
        String name = objectName.startsWith("/") ? objectName.substring(1) : objectName;
        return Map.of("url", presign(Method.GET, name, 10, TimeUnit.MINUTES));
    }

    private String presign(Method method, String objectName, int duration, TimeUnit unit) {
        try {
            return minio.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(method)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(duration, unit)
                            .build());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
