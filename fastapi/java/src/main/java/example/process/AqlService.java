package example.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AqlService {

    private static final TypeReference<List<Map<String, Object>>> ROWS = new TypeReference<>() {};

    private final CommandRunner runner;
    private final ObjectMapper mapper;

    public AqlService(CommandRunner runner, ObjectMapper mapper) {
        this.runner = runner;
        this.mapper = mapper;
    }

    /** Runs `aql query <statement>` and parses stdout as a JSON array of objects. */
    public List<Map<String, Object>> query(String statement) {
        String stdout = runner.run(List.of("aql", "query", statement));
        try {
            return mapper.readValue(stdout, ROWS);
        } catch (JsonProcessingException e) {
            throw new CommandFailedException("Invalid JSON returned by AQL: " + e.getOriginalMessage(), e);
        }
    }
}
