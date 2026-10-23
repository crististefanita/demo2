package atf.api.ui.network;

import atf.api.ui.config.UiNetworkInspectionConfiguration;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Generic Playwright boundary for bounded, typed API observations.
 *
 * <p>It is deliberately independent of Extent, business routes and test
 * assertions. Consumers can subscribe and adapt observations to their own
 * report contract; the inspector itself never creates report nodes or
 * decides a verdict.</p>
 */
public final class UiNetworkInspector implements AutoCloseable {
    private final UiNetworkInspectionConfiguration configuration;
    private final AtomicLong sequence = new AtomicLong();
    private final List<UiNetworkObservation> observations = new CopyOnWriteArrayList<>();
    private final List<Consumer<UiNetworkObservation>> subscribers = new CopyOnWriteArrayList<>();
    private Page page;
    private Consumer<Request> requestFinishedListener;
    private Consumer<Request> requestFailedListener;

    public UiNetworkInspector(UiNetworkInspectionConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
    }

    public UiNetworkInspectionConfiguration configuration() { return configuration; }

    public void attach(Page page) { attach(page, ignored -> { }); }

    public void attach(Page page, Consumer<UiNetworkObservation> subscriber) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(subscriber, "subscriber");
        if (!configuration.enabled()) return;
        if (this.page != null) throw new IllegalStateException("UI network inspector is already attached");
        this.page = page;
        subscribers.add(subscriber);
        requestFinishedListener = this::observeFinished;
        requestFailedListener = this::observeFailure;
        page.onRequestFinished(requestFinishedListener);
        page.onRequestFailed(requestFailedListener);
    }

    /** Allows a consumer adapter to subscribe after the Playwright session opened. */
    public void subscribe(Consumer<UiNetworkObservation> subscriber) {
        Objects.requireNonNull(subscriber, "subscriber");
        if (!configuration.enabled()) return;
        subscribers.add(subscriber);
    }

    /** Component-test seam and a safe adapter entry point for custom capture. */
    public Optional<UiNetworkObservation> capture(Response response) {
        if (!configuration.enabled() || response == null || response.request() == null
                || !configuration.accepts(response.url())) return Optional.empty();
        return capture(UiNetworkObservation.from(response, sequence.incrementAndGet(), configuration));
    }

    /**
     * Publishes an already typed observation through the same bounded path as
     * browser responses. Consumers and component fixtures use this seam when a
     * real Playwright response is intentionally outside the test boundary;
     * The inspector re-applies the configured redaction, bounding and
     * signature policy before storage or subscriber notification.
     */
    public Optional<UiNetworkObservation> capture(UiNetworkObservation observation) {
        if (!configuration.enabled() || observation == null
                || !configuration.accepts(observation.url())) return Optional.empty();
        if (observations.size() >= configuration.maxObservations()) return Optional.empty();
        UiNetworkObservation safeObservation = observation.sanitizedFor(configuration);
        observations.add(safeObservation);
        subscribers.forEach(subscriber -> notifySafely(subscriber, safeObservation));
        return Optional.of(safeObservation);
    }

    /** Returns lossless bounded observations in browser order. */
    public List<UiNetworkObservation> observations() {
        return List.copyOf(observations);
    }

    /** Capture this cursor before a UI action to exclude earlier read-backs. */
    public int observationCursor() {
        return observations.size();
    }

    /**
     * Returns the compact presentation stream while keeping raw observations
     * available for audit and signature counts. Consecutive identical
     * signatures are represented once when the configured policy enables it;
     * non-consecutive repetitions remain visible because they may belong to
     * different causal moments in the UI journey.
     */
    public List<UiNetworkObservation> presentationObservations() {
        if (!configuration.collapseConsecutiveSignatures()) return observations();
        List<UiNetworkObservation> compact = new ArrayList<>();
        String previousSignature = null;
        for (UiNetworkObservation observation : observations) {
            if (!Objects.equals(previousSignature, observation.requestSignature())) {
                compact.add(observation);
                previousSignature = observation.requestSignature();
            }
        }
        return List.copyOf(compact);
    }

    /** Returns the first observation for each signature plus its occurrence count. */
    public Map<String, Integer> signatureCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        observations.forEach(item -> counts.merge(item.requestSignature(), 1, Integer::sum));
        return Collections.unmodifiableMap(new LinkedHashMap<>(counts));
    }

    /** Searches the entire journal; use {@link #awaitAfter} for action read-back. */
    public Optional<UiNetworkObservation> await(Predicate<UiNetworkObservation> predicate,
                                                 Duration timeout) {
        return awaitAfter(0, predicate, timeout);
    }

    /** Waits only for observations captured after a cursor taken before the action. */
    public Optional<UiNetworkObservation> awaitAfter(int cursor,
                                                     Predicate<UiNetworkObservation> predicate,
                                                     Duration timeout) {
        Objects.requireNonNull(predicate, "predicate");
        if (!configuration.settlementPolicy().enabled()) {
            throw new IllegalStateException(
                    "UI network settlement is disabled; enable it explicitly before awaiting read-back");
        }
        if (cursor < 0 || cursor > observations.size()) {
            throw new IllegalArgumentException("UI network observation cursor is outside this journal");
        }
        Duration effective = timeout == null ? configuration.settlementPolicy().timeout() : timeout;
        if (effective.isZero() || effective.isNegative()) {
            throw new IllegalArgumentException("ui.network.await timeout must be positive");
        }
        long deadline = System.nanoTime() + effective.toNanos();
        int inspected = cursor;
        while (System.nanoTime() < deadline) {
            List<UiNetworkObservation> current = observations();
            for (int index = inspected; index < current.size(); index++) {
                UiNetworkObservation item = current.get(index);
                if (predicate.test(item)) return Optional.of(item);
            }
            inspected = current.size();
            if (page != null) {
                page.waitForTimeout(Math.max(1, configuration.settlementPolicy()
                        .pollInterval().toMillis()));
            } else {
                Thread.yield();
            }
        }
        return Optional.empty();
    }

    @Override public void close() {
        if (page != null) {
            if (requestFinishedListener != null) page.offRequestFinished(requestFinishedListener);
            if (requestFailedListener != null) page.offRequestFailed(requestFailedListener);
        }
        page = null;
        requestFinishedListener = null;
        requestFailedListener = null;
        subscribers.clear();
    }

    private void observeFinished(Request request) {
        if (!acceptsBrowserEvent(request)) return;
        Response response = request.response();
        if (response != null) capture(response);
    }

    private void observeFailure(Request request) {
        if (!acceptsBrowserEvent(request)) return;
        capture(UiNetworkObservation.fromFailure(request, sequence.incrementAndGet(), configuration));
    }

    private boolean acceptsBrowserEvent(Request request) {
        if (!configuration.enabled() || request == null) return false;
        try {
            return configuration.accepts(request.url());
        } catch (IllegalStateException malformedThirdPartyUrl) {
            // Browser events also include unrelated resources. An unparsable
            // third-party URL cannot match the explicit host allowlist and must
            // not abort a UI action or navigation while filtering that event.
            return false;
        }
    }

    private static void notifySafely(Consumer<UiNetworkObservation> subscriber,
                                     UiNetworkObservation observation) {
        try { subscriber.accept(observation); }
        catch (RuntimeException error) {
            throw new IllegalStateException(
                    "UI network observation subscriber failed for "
                            + observation.requestSignature(), error);
        }
    }
}
