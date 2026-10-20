package atf.ui.practiceshop.config;

import atf.api.core.config.ApiTransportMode;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/** Explicit API composition policy; acceptance remains the safe default. */
public record PracticeShopApiConfiguration(
        URI endpoint,
        ApiTransportMode mode,
        PracticeShopApiPrecondition precondition,
        Duration timeout) {

    public PracticeShopApiConfiguration {
        endpoint = Objects.requireNonNull(endpoint, "endpoint");
        mode = Objects.requireNonNull(mode, "mode");
        precondition = Objects.requireNonNull(precondition, "precondition");
        timeout = Objects.requireNonNull(timeout, "timeout");
        if (!endpoint.isAbsolute() || endpoint.getHost() == null) {
            throw new IllegalArgumentException("API endpoint must be an absolute URI");
        }
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("API timeout must be positive");
        }
    }

    public static PracticeShopApiConfiguration defaults() {
        return new PracticeShopApiConfiguration(
                URI.create("https://api.practicesoftwaretesting.com"),
                ApiTransportMode.ACCEPTANCE,
                PracticeShopApiPrecondition.AUTHENTICATED,
                Duration.ofSeconds(20));
    }

    public boolean authenticatedPrecondition() {
        return precondition == PracticeShopApiPrecondition.AUTHENTICATED;
    }
}
