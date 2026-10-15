package atf.api.ui.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** UI execution policy plus the report discovery policy used by UI checks. */
public record UiConfiguration(boolean enabled, UiBrowserConfiguration browser,
                              Path reportDirectory, String reportFilePrefix,
                              List<String> apiScenarioFamilies,
                              UiScreenValidationConfiguration screenValidation,
                              Optional<Path> auditInputReport) {
    static List<String> defaultScenarioFamilies() {
        return List.of("posts", "users", "requests", "headers", "lifecycle");
    }

    public UiConfiguration {
        browser = Objects.requireNonNull(browser, "browser");
        reportDirectory = Objects.requireNonNull(reportDirectory, "reportDirectory")
                .toAbsolutePath().normalize();
        reportFilePrefix = Objects.requireNonNull(reportFilePrefix, "reportFilePrefix").trim();
        if (reportFilePrefix.isEmpty()) throw new IllegalArgumentException("reportFilePrefix must not be blank");
        apiScenarioFamilies = List.copyOf(Objects.requireNonNull(apiScenarioFamilies, "apiScenarioFamilies"));
        if (apiScenarioFamilies.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException("apiScenarioFamilies must not contain blank values");
        }
        screenValidation = Objects.requireNonNull(screenValidation, "screenValidation");
        auditInputReport = Objects.requireNonNull(auditInputReport, "auditInputReport")
                .map(path -> path.toAbsolutePath().normalize());
    }

    public UiConfiguration(boolean enabled, UiBrowserConfiguration browser,
                           Path reportDirectory, String reportFilePrefix) {
        this(enabled, browser, reportDirectory, reportFilePrefix,
                defaultScenarioFamilies(),
                UiScreenValidationConfiguration.defaults(), Optional.empty());
    }

    public UiConfiguration(boolean enabled, UiBrowserConfiguration browser,
                           Path reportDirectory, String reportFilePrefix,
                           List<String> apiScenarioFamilies) {
        this(enabled, browser, reportDirectory, reportFilePrefix, apiScenarioFamilies,
                UiScreenValidationConfiguration.defaults(), Optional.empty());
    }

    /** Returns the same immutable UI policy with only the process override applied. */
    public UiConfiguration withEnabled(boolean enabled) {
        return new UiConfiguration(enabled, browser, reportDirectory, reportFilePrefix,
                apiScenarioFamilies, screenValidation, auditInputReport);
    }

    public UiConfiguration withAuditInputReport(Path report) {
        return new UiConfiguration(enabled, browser, reportDirectory, reportFilePrefix,
                apiScenarioFamilies, screenValidation, Optional.ofNullable(report));
    }

    public static UiConfiguration defaults(Path baseDirectory) {
        return new UiConfiguration(false, UiBrowserConfiguration.defaults(),
                baseDirectory.resolve("artifacts/reports"), "api-atf-extent-report_",
                defaultScenarioFamilies(),
                UiScreenValidationConfiguration.defaults(), Optional.empty());
    }
}
