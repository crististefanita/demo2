package atf.ui.tests.extent.support.config;

import atf.api.mock.AtfMockConfiguration;
import atf.api.mock.AtfMockConfigurationLoader;
import atf.api.mock.AtfMockTransport;
import atf.ui.tests.extent.support.config.output.UiReportOutputDirectory;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test-only ownership boundary for reporting fixtures.
 *
 * <p>The reporting module is independently testable: API fixtures use the
 * module-owned mock catalog and UI fixtures use the module-owned UI policy.
 * Explicit process overrides remain supported for IntelliJ and CI runs.</p>
 */
public final class ReportingTestResources {
    public static final String MOCK_RESOURCE = "atf-mock.yaml";
    public static final String UI_RESOURCE = "atf-reporting-ui.yaml";

    private ReportingTestResources() { }

    public static Path resourcePath(String resourceName) {
        URL resource = ReportingTestResources.class.getClassLoader().getResource(resourceName);
        if (resource == null) {
            throw new IllegalStateException("Reporting test resource is missing: " + resourceName);
        }
        try {
            Path path = Path.of(resource.toURI()).toAbsolutePath().normalize();
            if (!Files.isRegularFile(path)) {
                throw new IllegalStateException("Reporting test resource is not a file: " + path);
            }
            return path;
        } catch (URISyntaxException | RuntimeException error) {
            throw new IllegalStateException("Cannot resolve reporting test resource: " + resourceName, error);
        }
    }

    public static AtfMockConfiguration mockConfiguration() {
        return AtfMockConfigurationLoader.load(resourcePath(MOCK_RESOURCE));
    }

    public static AtfMockTransport mockTransport() {
        return new AtfMockTransport(mockConfiguration());
    }

    public static UiConfiguration uiConfiguration() {
        return uiConfiguration(null);
    }

    public static UiConfiguration uiConfiguration(Class<?> testClass) {
        AtfUiConfigurationContext process = AtfUiConfigurationContext.fromProcess();
        UiConfiguration loaded = process.explicitConfigPath() == null
                ? AtfUiConfigurationLoader.load(resourcePath(UI_RESOURCE))
                : AtfUiConfigurationLoader.load(process);
        Path reportOverride = process.reportOverridePath();
        if (reportOverride != null) {
            loaded = loaded.withAuditInputReport(reportOverride);
        }
        Path directory = UiReportOutputDirectory.resolve(projectArtifactDirectory(process), testClass);
        return loaded.withReportDirectory(directory);
    }

    private static Path projectArtifactDirectory(AtfUiConfigurationContext process) {
        Path current = process.startDirectory();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("atf-reporting/pom.xml"))) {
                return current.resolve("artifacts/reports/ui").toAbsolutePath().normalize();
            }
            if ("atf-reporting".equalsIgnoreCase(String.valueOf(current.getFileName()))
                    && current.getParent() != null
                    && Files.isRegularFile(current.getParent().resolve("atf-reporting/pom.xml"))) {
                return current.getParent().resolve("artifacts/reports/ui").toAbsolutePath().normalize();
            }
            current = current.getParent();
        }
        return process.startDirectory().resolve("artifacts/reports/ui").toAbsolutePath().normalize();
    }
}
