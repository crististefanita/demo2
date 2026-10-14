package atf.api.core.http;

/** Semantic result of one completed transport attempt. */
public enum ApiAttemptOutcome {
    SUCCESS,
    RETRYABLE_RESPONSE,
    RETRYABLE_FAILURE
}
