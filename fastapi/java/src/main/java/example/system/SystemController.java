package example.system;

import example.model.Column;
import example.security.InternalKeyVerifier;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final SystemService system;
    private final InternalKeyVerifier keys;

    public SystemController(SystemService system, InternalKeyVerifier keys) {
        this.system = system;
        this.keys = keys;
    }

    @GetMapping("/list")
    public Map<String, List<String>> list(
            @RequestHeader(value = InternalKeyVerifier.HEADER, required = false) String key) {
        keys.verify(key);
        return Map.of("result", SystemService.SOURCES);
    }

    @GetMapping("/fields/{source}")
    public Map<String, List<String>> fields(
            @PathVariable String source,
            @RequestHeader(value = InternalKeyVerifier.HEADER, required = false) String key) {
        keys.verify(key);
        return Map.of("fields", system.fields(source));
    }

    @GetMapping("/{source}")
    public SourceResponse source(
            @PathVariable String source,
            @RequestHeader(value = InternalKeyVerifier.HEADER, required = false) String key) {
        keys.verify(key);
        return new SourceResponse(system.columns(source), system.rows(source));
    }

    public record SourceResponse(List<Column> columns, List<Map<String, Object>> results) {}
}
