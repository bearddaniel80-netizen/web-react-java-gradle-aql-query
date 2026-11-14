package example.query;

import example.model.Column;
import example.model.QueryJob;
import example.process.AqlService;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;

@Service
public class QueryJobService {

    // Demo storage. Use Redis or a database in production.
    private final Map<String, QueryJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final AqlService aql;

    public QueryJobService(AqlService aql) {
        this.aql = aql;
    }

    public QueryJob submit(String query) {
        String jobId = UUID.randomUUID().toString();
        QueryJob job = QueryJob.pending(jobId);
        jobs.put(jobId, job);
        executor.submit(() -> execute(jobId, query));
        return job;
    }

    public Optional<QueryJob> find(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    private void execute(String jobId, String query) {
        jobs.computeIfPresent(jobId, (id, job) -> job.running());
        try {
            List<Map<String, Object>> rows = aql.query(query);
            List<Column> columns = columnsOf(rows);
            jobs.computeIfPresent(jobId, (id, job) -> job.completed(rows, columns));
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.toString();
            jobs.computeIfPresent(jobId, (id, job) -> job.failed(message));
        }
    }

    /** Column metadata inferred from the first row. */
    private static List<Column> columnsOf(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Column> columns = new ArrayList<>();
        rows.get(0).forEach((key, value) -> columns.add(Column.of(key, dataType(value))));
        return columns;
    }

    private static String dataType(Object value) {
        if (value instanceof Boolean) return "boolean";
        if (value instanceof Number) return "number";
        if (value instanceof String) return "string";
        return "unknown";
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }
}
