package atf.api.core.http;

    /** Infrastructure failure with a preserved cause for report diagnostics. */
public final class ApiExecutionException extends RuntimeException {
    public ApiExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
