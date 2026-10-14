package atf.api.core.http;

/** Eroare de infrastructură cu cauză păstrată pentru diagnosticul raportului. */
public final class ApiExecutionException extends RuntimeException {
    public ApiExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
