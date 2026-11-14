package example.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;

/** Runs an external command and returns its stdout. Blocking: call from a (virtual) request thread. */
@Component
public class CommandRunner {

    public String run(List<String> command) {
        try {
            Process process = new ProcessBuilder(command).start();
            process.getOutputStream().close();

            // Drain stderr concurrently so a full pipe can't deadlock the process
            CompletableFuture<String> stderr =
                    CompletableFuture.supplyAsync(() -> readAll(process.getErrorStream()));
            String stdout = readAll(process.getInputStream());

            int exitCode = process.waitFor();
            String err = stderr.join();

            if (exitCode != 0) {
                throw new CommandFailedException(
                        err.isBlank() ? "Command failed: " + command.get(0) : err);
            }
            return stdout;
        } catch (IOException e) {
            throw new CommandFailedException(
                    "Unable to start " + command.get(0) + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandFailedException("Interrupted while running " + command.get(0), e);
        }
    }

    private static String readAll(InputStream in) {
        try (in) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
