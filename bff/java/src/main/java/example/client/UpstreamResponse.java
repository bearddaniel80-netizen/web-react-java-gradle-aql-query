package example.client;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Raw upstream reply: status, content type and body exactly as FastAPI sent them (JSON or HTML). */
public record UpstreamResponse(int status, MediaType contentType, String body) {

    public ResponseEntity<String> toResponse() {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);
        if (contentType != null) {
            builder.contentType(contentType);
        }
        return builder.body(body);
    }
}
