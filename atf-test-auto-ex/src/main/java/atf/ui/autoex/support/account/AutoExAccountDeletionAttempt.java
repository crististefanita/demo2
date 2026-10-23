package atf.ui.autoex.support.account;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;

import java.net.URI;
import java.util.Objects;
import java.util.function.Consumer;

/** Scoped, passive guard: any request to this store's deletion endpoint prevents navigation replay. */
public final class AutoExAccountDeletionAttempt implements AutoCloseable {
    private final Page page;
    private final URI endpoint;
    private final Consumer<Request> listener = this::observe;
    private int startedRequests;

    public AutoExAccountDeletionAttempt(Page page, URI endpoint) {
        this.page = Objects.requireNonNull(page, "page");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        page.onRequest(listener);
    }

    private void observe(Request request) {
        URI destination = URI.create(request.url());
        if (endpoint.getScheme().equalsIgnoreCase(destination.getScheme())
                && endpoint.getRawAuthority().equalsIgnoreCase(destination.getRawAuthority())
                && endpoint.getRawPath().equals(destination.getRawPath())) {
            startedRequests++;
        }
    }

    public int startedRequests() { return startedRequests; }

    public AutoExAccountDeletionProgress progressAfter(boolean vignetteDismissed) {
        return new AutoExAccountDeletionProgress(vignetteDismissed, startedRequests,
                endpoint.toString().equals(page.url()));
    }

    @Override
    public void close() { page.offRequest(listener); }
}
