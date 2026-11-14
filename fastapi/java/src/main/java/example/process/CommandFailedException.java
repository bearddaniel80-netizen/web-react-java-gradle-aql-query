package example.process;

public class CommandFailedException extends RuntimeException {
    public CommandFailedException(String message) {
        super(message);
    }

    public CommandFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
