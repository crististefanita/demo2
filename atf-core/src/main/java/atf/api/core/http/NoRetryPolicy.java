package atf.api.core.http;

public final class NoRetryPolicy {
    public boolean shouldRetry(ApiResponse response, int attempt) {
        return false;
    }
}
