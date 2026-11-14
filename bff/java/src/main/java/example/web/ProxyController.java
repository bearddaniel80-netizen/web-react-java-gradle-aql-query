package example.web;

import com.fasterxml.jackson.databind.JsonNode;
import example.client.FastApiClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProxyController {

    private final FastApiClient fastApi;

    public ProxyController(FastApiClient fastApi) {
        this.fastApi = fastApi;
    }

    // Dynamic routes
    @GetMapping("/system/fields/{source}")
    public ResponseEntity<String> systemFields(@PathVariable String source) {
        return fastApi.get("/api/system/fields/{source}", source).toResponse();
    }

    @GetMapping("/system/{source}")
    public ResponseEntity<String> system(@PathVariable String source) {
        return fastApi.get("/api/system/{source}", source).toResponse();
    }

    @GetMapping("/query/results/{jobId}")
    public ResponseEntity<String> queryResults(@PathVariable String jobId) {
        return fastApi.get("/api/query/results/{jobId}", jobId).toResponse();
    }

    @PostMapping("/query/submit/{query}")
    public ResponseEntity<String> querySubmit(
            @PathVariable String query,
            @RequestBody(required = false) JsonNode body) {
        return fastApi.post("/api/query/submit/{query}", body, query).toResponse();
    }

    // Static routes (Spring prefers literal matches over /system/{source})
    @GetMapping("/data")
    public ResponseEntity<String> data() {
        return fastApi.get("/api/data").toResponse();
    }

    @GetMapping("/index")
    public ResponseEntity<String> index() {
        return fastApi.get("/api/index").toResponse();
    }

    @GetMapping("/system/list")
    public ResponseEntity<String> systemList() {
        return fastApi.get("/api/system/list").toResponse();
    }
}
