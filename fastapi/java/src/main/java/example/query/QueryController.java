package example.query;

import example.model.QueryJob;
import example.security.InternalKeyVerifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private final QueryJobService jobs;
    private final InternalKeyVerifier keys;

    public QueryController(QueryJobService jobs, InternalKeyVerifier keys) {
        this.jobs = jobs;
        this.keys = keys;
    }

    @PostMapping("/submit/{query}")
    public SubmitResponse submit(
            @PathVariable String query,
            @RequestHeader(value = InternalKeyVerifier.HEADER, required = false) String key) {
        keys.verify(key);
        QueryJob job = jobs.submit(query);
        return new SubmitResponse(job.jobId(), job.status());
    }

    @GetMapping("/results/{jobId}")
    public QueryJob results(
            @PathVariable String jobId,
            @RequestHeader(value = InternalKeyVerifier.HEADER, required = false) String key) {
        keys.verify(key);
        return jobs.find(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Query job not found"));
    }

    public record SubmitResponse(String jobId, String status) {}
}
