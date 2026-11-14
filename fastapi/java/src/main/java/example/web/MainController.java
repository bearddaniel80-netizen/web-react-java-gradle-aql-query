package example.web;

import example.process.CommandRunner;
import example.security.InternalKeyVerifier;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    private final InternalKeyVerifier keys;
    private final CommandRunner runner;

    public MainController(InternalKeyVerifier keys, CommandRunner runner) {
        this.keys = keys;
        this.runner = runner;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/api/data")
    public Map<String, String> data(
            @RequestHeader(value = InternalKeyVerifier.HEADER, required = false) String key) {
        keys.verify(key);
        return Map.of("message", "Hello from Spring Boot");
    }

    @GetMapping("/api/index")
    public ResponseEntity<String> readme(
            @RequestHeader(value = InternalKeyVerifier.HEADER, required = false) String key) {
        keys.verify(key);
        Path readme = Path.of("README.md").toAbsolutePath();
        String html = runner.run(List.of(
                "pandoc", "-f", "markdown-yaml_metadata_block", readme.toString(), "-t", "html"));
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
