package example.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InternalKeyVerifier {

    public static final String HEADER = "X-Internal-Key";

    private final byte[] expected;

    public InternalKeyVerifier(SecretReader secrets) {
        this.expected = secrets.read("fastapi_internal_key").getBytes(StandardCharsets.UTF_8);
    }

    public void verify(String provided) {
        // Constant-time comparison
        if (provided == null
                || !MessageDigest.isEqual(expected, provided.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
    }
}
