package example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UploadUrlResponse(
        @JsonProperty("object_name") String objectName,
        String url) {
}
