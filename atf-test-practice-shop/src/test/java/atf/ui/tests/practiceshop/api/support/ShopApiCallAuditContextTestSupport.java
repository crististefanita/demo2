package atf.ui.tests.practiceshop.api.support;

import atf.api.core.http.response.ApiExecutionResult;

import java.util.Objects;
import java.util.function.Consumer;

/** Test-thread scope used by deterministic transports to expose their actual exchanges. */
public final class ShopApiCallAuditContextTestSupport {
    private static final ThreadLocal<Consumer<ApiExecutionResult>> RECORDER = new ThreadLocal<>();

    private ShopApiCallAuditContextTestSupport() { }

    public static void recordWith(Consumer<ApiExecutionResult> recorder) {
        RECORDER.set(Objects.requireNonNull(recorder, "recorder"));
    }

    public static Consumer<ApiExecutionResult> currentRecorder() {
        return RECORDER.get();
    }

    public static void clear() {
        RECORDER.remove();
    }
}
