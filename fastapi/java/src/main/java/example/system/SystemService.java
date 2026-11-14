package example.system;

import example.model.Column;
import example.process.AqlService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SystemService {

    public static final List<String> SOURCES = List.of(
            "aggregates", "docs", "docs_filter", "operators", "scalars",
            "stdin_sources", "table_functions", "transformers");

    private final AqlService aql;

    public SystemService(AqlService aql) {
        this.aql = aql;
    }

    public List<String> fields(String source) {
        return describe(source).stream()
                .map(column -> String.valueOf(column.get("name")))
                .toList();
    }

    public List<Column> columns(String source) {
        return describe(source).stream()
                .map(column -> Column.of(
                        String.valueOf(column.get("name")),
                        String.valueOf(column.get("type"))))
                .toList();
    }

    public List<Map<String, Object>> rows(String source) {
        return aql.query("SHOW " + validate(source));
    }

    private List<Map<String, Object>> describe(String source) {
        return aql.query("DESCRIBE " + validate(source));
    }

    /** Only whitelisted sources reach the command line. */
    private static String validate(String source) {
        if (!SOURCES.contains(source)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown source: " + source);
        }
        return source;
    }
}
