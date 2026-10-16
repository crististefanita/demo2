package atf.api.tests.support.junit;

import atf.api.core.http.ApiRequest;
import atf.api.core.http.ApiTransport;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/** Captures the semantic request without changing the configured transport. */
public final class RecordingApiTransport implements ApiTransport {
    private final ApiTransport delegate;
    private final AtomicReference<ApiRequest> lastRequest = new AtomicReference<>();

    public RecordingApiTransport(ApiTransport delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public CompletableFuture<atf.api.core.http.ApiResponse> send(ApiRequest request,
                                                                   String url,
                                                                   String authProfile) {
        lastRequest.set(request);
        return delegate.send(request, url, authProfile);
    }

    public ApiRequest lastRequest() {
        return lastRequest.get();
    }
}
