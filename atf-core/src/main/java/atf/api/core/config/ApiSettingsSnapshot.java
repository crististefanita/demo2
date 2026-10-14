package atf.api.core.config;

import java.net.URI;
import java.time.Duration;
import java.util.Set;

/** Copie immutable folosită de un executor; host-ul nu o poate modifica. */
public record ApiSettingsSnapshot(URI baseUri, String defaultAuthProfile, Duration timeout,
                                  Set<Integer> retryStatuses, int maxAttempts) {
    public ApiSettingsSnapshot {
        retryStatuses = retryStatuses == null ? Set.of() : Set.copyOf(retryStatuses);
    }
}
