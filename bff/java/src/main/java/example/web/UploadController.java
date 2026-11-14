package example.web;

import example.client.FastApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    private final FastApiClient fastApi;

    public UploadController(FastApiClient fastApi) {
        this.fastApi = fastApi;
    }

    @PostMapping(path = "/api/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestPart(name = "file", required = false) MultipartFile file,
            @RequestParam(name = "description", required = false) String description) {

        if (file == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded"));
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("filename", file.getOriginalFilename());
            body.put("content_type", file.getContentType());
            if (description != null) {
                body.put("description", description);
            }
            return fastApi.post("/api/uploads", body).toResponse();
        } catch (Exception e) {
            log.error("Upload failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Upload failed"));
        }
    }
}
