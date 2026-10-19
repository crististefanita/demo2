package atf.api.core.http.execution;
import atf.api.core.http.response.ApiResponse;

public final class NoRetryPolicy {
    public boolean shouldRetry(ApiResponse response, int attempt) {
        return false;
    }
}
