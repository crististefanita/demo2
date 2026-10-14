package atf.api.core.config;

import java.util.Objects;

/** Boundary minim de configurare pentru un modul ATF. */
public record AtfConfiguration(ApiSettings api, ReportingSettings reporting) {
    public AtfConfiguration {
        Objects.requireNonNull(api, "api");
        Objects.requireNonNull(reporting, "reporting");
    }

    public static AtfConfiguration defaults(java.net.URI baseUri) {
        return new AtfConfiguration(new ApiSettings(baseUri, "none"), ReportingSettings.defaults());
    }

    public void validate() {
        api.snapshot();
        if (reporting.assertionMaxDisplayCharacters() <= 0) {
            throw new IllegalArgumentException("reporting display limit must be positive");
        }
    }
}
