package example.security;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

/** Reads a Docker secret from /run/secrets/{name}, falling back to an env var (local dev). */
@Component
public class SecretReader {

    private static final Path SECRETS_DIR = Path.of("/run/secrets");

    public String read(String name) {
        Path path = SECRETS_DIR.resolve(name);
        try {
            if (Files.exists(path)) {
                return Files.readString(path).strip();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read secret " + name, e);
        }
        String value = System.getenv(name);
        if (value == null) {
            throw new IllegalStateException("Missing secret: " + name);
        }
        return value;
    }
}
