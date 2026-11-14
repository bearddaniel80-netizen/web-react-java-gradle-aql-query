package example.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QueryJob(
        String jobId,
        String status,
        List<Map<String, Object>> results,
        List<Column> columns,
        String error) {

    public static QueryJob pending(String jobId) {
        return new QueryJob(jobId, "pending", List.of(), List.of(), null);
    }

    public QueryJob running() {
        return new QueryJob(jobId, "running", results, columns, null);
    }

    public QueryJob completed(List<Map<String, Object>> results, List<Column> columns) {
        return new QueryJob(jobId, "completed", results, columns, null);
    }

    public QueryJob failed(String error) {
        return new QueryJob(jobId, "failed", results, columns, error);
    }
}
