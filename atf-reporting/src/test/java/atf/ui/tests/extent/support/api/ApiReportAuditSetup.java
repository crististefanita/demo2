package atf.ui.tests.extent.support.api;
import atf.ui.tests.extent.support.config.ReportingTestResources;
import atf.api.core.config.source.ApiBootstrapConfigurationLoader;
import atf.api.core.testing.api.ApiScenarioSelection;
import atf.api.core.testing.api.ApiScenarioSetup;
import atf.api.tests.support.setup.ApiScenarioSetupCatalog;
import atf.ui.tests.extent.support.data.UiTestData;

import atf.api.execution.ApiModule;
import atf.api.execution.ApiModuleFactory;
import atf.api.core.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.mock.AtfMockConfiguration;
import atf.api.mock.AtfMockTransport;

import atf.api.reporting.extent.session.ExtentReportSession;
import atf.ui.tests.extent.support.config.UiConfiguration;
import atf.ui.tests.extent.support.config.UiAuditSuiteConfiguration;
import atf.ui.tests.extent.component.plan.ApiScenarioManifest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Composition root for a read-only API artifact consumed by a UI test.
 *
 * <p>The setup source is deliberately small: the test declares either API
 * families or exact {@link ApiScenarioSetup#tests()} references and the
 * catalog publishes them through the project mock. No browser selector or UI
 * assertion is allowed to leak into this layer.</p>
 */
public class ApiReportAuditSetup {
    public Result generate(Class<?> setupOwner, UiConfiguration configuration) {
        Objects.requireNonNull(setupOwner, "setupOwner");
        Objects.requireNonNull(configuration, "configuration");
        return generate(setupOwner, configuration, families(setupOwner, configuration));
    }

    /** Generates from an already resolved immutable spec without re-reading its source. */
    public Result generate(Class<?> setupOwner, UiConfiguration configuration,
                           Collection<String> selectedFamilies) {
        Objects.requireNonNull(setupOwner, "setupOwner");
        Objects.requireNonNull(configuration, "configuration");
        List<ApiScenarioSelection> selectedTests = selectedTests(setupOwner);
        if (!selectedTests.isEmpty()) {
            return generateSelected(setupOwner, configuration, selectedTests);
        }
        List<String> families = parseFamilies(String.join(",", selectedFamilies),
                "UiReportAuditSpec.apiFamilies");
        AtfMockTransport transport = ReportingTestResources.mockTransport();
        ApiModule api = acceptanceApi(transport);
        Path report;
        try (ExtentReportSession session = ExtentReportSession.openConfigured()) {
            report = session.reportPath();
            ApiScenarioSetupCatalog.publish(session, api,
                    new ApiScenarioLifecycleRunner(), families);
        }
        if (!Files.isRegularFile(report)) {
            throw new IllegalStateException("API setup did not publish an Extent report: " + report);
        }
        return new Result(report, transport.configuration(), families,
                ApiScenarioManifest.from(families, transport.configuration()), List.of());
    }

    /**
     * Resolves one reusable API collection. Manual JVM selection has the
     * highest precedence, followed by the test declaration and YAML.
     */
    public static List<String> families(Class<?> setupOwner, UiConfiguration configuration) {
        List<ApiScenarioSelection> selectedTests = selectedTests(setupOwner);
        if (!selectedTests.isEmpty()) {
            return ApiScenarioSetupCatalog.familiesFor(selectedTests);
        }
        String manual = System.getProperty(UiTestData.API_FAMILIES_PROPERTY);
        if (manual != null) return parseFamilies(manual, UiTestData.API_FAMILIES_PROPERTY);
        String suite = System.getProperty(UiTestData.AUDIT_SUITE_PROPERTY);
        if (suite != null && !suite.isBlank()) {
            return parseFamilies(suiteFamilies(suite, configuration), UiTestData.AUDIT_SUITE_PROPERTY);
        }
        ApiScenarioSetup declaration = setupOwner.getAnnotation(ApiScenarioSetup.class);
        if (declaration == null || declaration.families().length == 0) {
            return configuration.apiScenarioFamilies();
        }
        return parseFamilies(String.join(",", declaration.families()), "@ApiScenarioSetup.families");
    }

    /** Resolves exact annotation references before any family or YAML fallback. */
    public static List<ApiScenarioSelection> selectedTests(Class<?> setupOwner) {
        Objects.requireNonNull(setupOwner, "setupOwner");
        ApiScenarioSetup declaration = setupOwner.getAnnotation(ApiScenarioSetup.class);
        if (declaration == null || declaration.tests().length == 0) return List.of();
        return ApiScenarioSetupCatalog.resolve(declaration.tests());
    }

    private Result generateSelected(Class<?> setupOwner, UiConfiguration configuration,
                                    List<ApiScenarioSelection> selectedTests) {
        List<String> families = ApiScenarioSetupCatalog.familiesFor(selectedTests);
        AtfMockTransport transport = ReportingTestResources.mockTransport();
        ApiModule api = acceptanceApi(transport);
        Path report;
        try (ExtentReportSession session = ExtentReportSession.openConfigured()) {
            report = session.reportPath();
            ApiScenarioSetupCatalog.publishSelected(session, api,
                    new ApiScenarioLifecycleRunner(), selectedTests);
        }
        if (!Files.isRegularFile(report)) {
            throw new IllegalStateException("Selected API scenarios did not publish an Extent report: "
                    + report);
        }
        return new Result(report, transport.configuration(), families,
                ApiScenarioManifest.from(families, transport.configuration(), selectedTests),
                selectedTests);
    }

    private static String suiteFamilies(String suite, UiConfiguration configuration) {
        UiAuditSuiteConfiguration configured = configuration.auditSuite(suite).orElse(null);
        if (configured != null) return String.join(",", configured.scenarioFamilies());
        return switch (suite.trim().toLowerCase(Locale.ROOT)) {
            case "users-posts", "users_posts" -> "users,posts";
            case "requests-headers", "requests_headers" -> "requests,headers";
            case "all", "all-configured", "all_configured" ->
                    String.join(",", configuration.apiScenarioFamilies());
            default -> suite;
        };
    }

    private static ApiModule acceptanceApi(AtfMockTransport transport) {
        var bootstrap = ApiBootstrapConfigurationLoader.load();
        if (bootstrap.transportPolicy().mode() != atf.api.core.config.ApiTransportMode.ACCEPTANCE) {
            throw new IllegalStateException("UI API setup requires the acceptance transport; live is never implicit");
        }
        return ApiModuleFactory.bootstrap(bootstrap, transport, null);
    }

    private static List<String> parseFamilies(String raw, String source) {
        List<String> parsed = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
        if (parsed.isEmpty()) {
            throw new IllegalArgumentException(source + " must select at least one API family");
        }
        return parsed;
    }

    public record Result(Path report, AtfMockConfiguration mock, List<String> families,
                         ApiScenarioManifest manifest,
                         List<ApiScenarioSelection> selectedTests) {
        public Result(Path report, AtfMockConfiguration mock, List<String> families) {
            this(report, mock, families, ApiScenarioManifest.from(families, mock), List.of());
        }

        public Result(Path report, AtfMockConfiguration mock, List<String> families,
                      ApiScenarioManifest manifest) {
            this(report, mock, families, manifest, List.of());
        }

        public Result {
            report = Objects.requireNonNull(report, "report").toAbsolutePath().normalize();
            mock = Objects.requireNonNull(mock, "mock");
            families = List.copyOf(Objects.requireNonNull(families, "families"));
            manifest = Objects.requireNonNull(manifest, "manifest");
            selectedTests = List.copyOf(Objects.requireNonNull(selectedTests, "selectedTests"));
        }
    }
}
