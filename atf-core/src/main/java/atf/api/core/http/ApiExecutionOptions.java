package atf.api.core.http;

import java.time.Duration;

/** Bugetul per execuție; este immutable și nu schimbă configurația globală. */
public record ApiExecutionOptions(Duration timeout) {
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    public ApiExecutionOptions {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
    }

    public static ApiExecutionOptions defaults() {
        return new ApiExecutionOptions(DEFAULT_TIMEOUT);
    }
}
