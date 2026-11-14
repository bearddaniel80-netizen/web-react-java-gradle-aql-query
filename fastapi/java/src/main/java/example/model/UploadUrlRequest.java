package example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UploadUrlRequest {

    @JsonProperty("content_type")
    private String contentType;

    private String filename;
}
