package atf.ui.tests.autoex.api;

import atf.api.console.junit.AtfConsoleTestExtension;
import atf.api.core.http.response.ApiExecutionResult;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.model.diagnostic.ReportPhaseDiagnostic;
import atf.api.reporting.api.evidence.ApiExchangeEvidence;
import atf.api.reporting.api.ReportModelAdapters;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.configuration.ReportingSettings;
import atf.api.reporting.configuration.ReportEnvironmentField;
import atf.api.reporting.configuration.ReportEnvironmentSnapshot;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.reporting.security.SensitiveDataRedactor;
import atf.ui.autoex.api.AutoExApiJsonField;
import atf.ui.autoex.support.AutoExProjectRoot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/** Publishes direct Automation Exercise API contract runs as one auditable Extent artifact. */
public final class AutoExApiContractReportExtension implements BeforeAllCallback, BeforeEachCallback,
        AfterEachCallback, AfterAllCallback {
    public static final String REPORT_DIRECTORY_PROPERTY = "atf.autoex.api.report-directory";
    private static final String DEFAULT_REPORT_DIRECTORY = "artifacts/reports/api/auto-ex";
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(AutoExApiContractReportExtension.class);
    private static final String SESSION = "session";
    private static final String RUN = "run";
    private static final ThreadLocal<Run> CURRENT = new ThreadLocal<>();
    private static final ObjectMapper JSON = new ObjectMapper();

    private record Run(AtfReportPorts.Scenario scenario, AtfReportPorts.Phase phase) { }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Path root = AutoExProjectRoot.discover();
        Path directory = resolveDirectory(root,
                System.getProperty(REPORT_DIRECTORY_PROPERTY, DEFAULT_REPORT_DIRECTORY));
        Files.createDirectories(directory);
        String timestamp = Instant.now().toString().replace(":", "-");
        Path report = directory.resolve("api-atf-auto-ex-contract_" + timestamp + "_"
                + UUID.randomUUID().toString().substring(0, 8) + ".html");
        var environment = ReportEnvironmentSnapshot.builder()
                .put(ReportEnvironmentField.ENVIRONMENT, "LIVE OPT-IN")
                .put(ReportEnvironmentField.TEST_PACKAGE, context.getRequiredTestClass().getPackageName())
                .put(ReportEnvironmentField.SUITE, "Automation Exercise API contract")
                .put(ReportEnvironmentField.PROFILE, "auto-ex-api")
                .put(ReportEnvironmentField.TRANSPORT_MODE, "LIVE HTTP")
                .put(ReportEnvironmentField.REPORTING, "Extent HTML")
                .put(ReportEnvironmentField.REPORT_ARTIFACT, "Direct API contract evidence")
                .build();
        var session = new ExtentReportSession(report, ReportingSettings.defaults(),
                new SensitiveDataRedactor(),
                reportPathSink(context.getRequiredTestClass()), environment);
        context.getStore(NAMESPACE).put(SESSION, session);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        var session = context.getStore(NAMESPACE).get(SESSION, ExtentReportSession.class);
        if (session == null) throw new IllegalStateException("API contract report session was not opened");
        Class<?> owner = context.getRequiredTestClass();
        var scenario = session.startScenario(owner.getSimpleName(), owner.getPackageName(),
                context.getDisplayName(), List.of("api", "auto-ex", "contract", "live"));
        var run = new Run(scenario, scenario.startPhase("TEST BODY"));
        context.getStore(NAMESPACE).put(RUN, run);
        CURRENT.set(run);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Run run = context.getStore(NAMESPACE).remove(RUN, Run.class);
        CURRENT.remove();
        if (run == null) return;
        Throwable failure = context.getExecutionException().orElse(null);
        AtfReportStatus status = failure == null ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
        try {
            if (failure != null) recordExecutionFailure(run.phase(), failure);
            run.phase().complete(status);
            run.scenario().complete(status);
        } finally {
            run.phase().close();
            run.scenario().close();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        ExtentReportSession session = context.getStore(NAMESPACE).remove(SESSION, ExtentReportSession.class);
        if (session != null) session.close();
    }

    public static void publish(String title, ApiExecutionResult execution, int expectedResponseCode,
                               String expectedMessage) {
        Run run = CURRENT.get();
        if (run == null) throw new IllegalStateException("API exchange was recorded outside its JUnit report scope");
        publish(run.phase(), title, execution, expectedResponseCode, expectedMessage);
    }

    public static void publish(String title, ApiExecutionResult execution, int expectedResponseCode,
                               String expectedMessage, Collection<AtfAssertion> additionalAssertions) {
        Run run = CURRENT.get();
        if (run == null) throw new IllegalStateException("API exchange was recorded outside its JUnit report scope");
        publish(run.phase(), title, execution, expectedResponseCode, expectedMessage, additionalAssertions);
    }

    public static void publish(AtfReportPorts.Phase phase, String title, ApiExecutionResult execution,
                               int expectedResponseCode, String expectedMessage) {
        publish(phase, title, execution, expectedResponseCode, expectedMessage, List.of());
    }

    public static void publish(AtfReportPorts.Phase phase, String title, ApiExecutionResult execution,
                               int expectedResponseCode, String expectedMessage,
                               Collection<AtfAssertion> additionalAssertions) {
        AtfReportPorts.Request node = phase.startRequest(title,
                execution.request().method(), execution.request().path());
        ApiExchangeEvidence.publish(node, ReportModelAdapters.execution(execution));

        String actualCode = jsonField(execution.response().body(), AutoExApiJsonField.RESPONSE_CODE.path());
        String actualMessage = jsonField(execution.response().body(), AutoExApiJsonField.MESSAGE.path());
        boolean codeMatches = Integer.toString(expectedResponseCode).equals(actualCode);
        boolean messageMatches = expectedMessage == null || expectedMessage.equals(actualMessage);
        AtfReportStatus status = codeMatches && messageMatches
                ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
        var validation = node.startValidation("Validate documented JSON response contract");
        var assertions = new java.util.ArrayList<AtfAssertion>();
        assertions.add(new AtfAssertion("JSON responseCode", Integer.toString(expectedResponseCode),
                actualCode, codeMatches));
        if (expectedMessage != null) {
            assertions.add(new AtfAssertion("JSON message", expectedMessage, actualMessage, messageMatches));
        }
        assertions.addAll(additionalAssertions);
        status = assertions.stream().allMatch(AtfAssertion::passed)
                ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
        validation.assertions(assertions);
        validation.complete(status);
        node.complete(status);
    }

    public static void recordExecutionFailure(AtfReportPorts.Phase phase, Throwable failure) {
        if (phase == null || failure == null) return;
        phase.diagnostic(ReportPhaseDiagnostic.Kind.JUNIT_FAILURE, describeFailure(failure));
    }

    private static String describeFailure(Throwable failure) {
        var description = new StringBuilder();
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        var pending = new ArrayDeque<Throwable>();
        pending.add(failure);
        int relatedFailures = 0;
        while (!pending.isEmpty() && relatedFailures < 4) {
            Throwable current = pending.removeFirst();
            if (!seen.add(current)) continue;
            relatedFailures++;
            if (!description.isEmpty()) description.append(" <- ");
            description.append(current.getClass().getSimpleName());
            String message = current.getMessage();
            if (message != null && !message.isBlank()) description.append(": ").append(message.trim());
            if (current.getCause() != null) pending.addLast(current.getCause());
            for (Throwable suppressed : current.getSuppressed()) pending.addLast(suppressed);
        }
        if (description.length() > 1_000) description.setLength(1_000);
        return description.toString();
    }

    public static Path resolveDirectory(Path projectRoot, String configured) {
        Path relative = Path.of(configured == null || configured.isBlank()
                ? DEFAULT_REPORT_DIRECTORY : configured).normalize();
        if (relative.isAbsolute() || relative.startsWith("..") || relative.toString().isBlank()) {
            throw new IllegalArgumentException(REPORT_DIRECTORY_PROPERTY
                    + " must be a non-empty path inside the project");
        }
        return projectRoot.toAbsolutePath().normalize().resolve(relative).normalize();
    }

    private static String jsonField(String json, String field) {
        try {
            var node = JSON.readTree(json);
            if (node == null || !node.has(field)) return "<missing>";
            return node.path(field).asText();
        } catch (java.io.IOException invalidJson) {
            return "<invalid JSON>";
        }
    }

    private static atf.api.reporting.console.AtfConsoleSink reportPathSink(Class<?> owner) {
        var coreSink = AtfConsoleTestExtension.reportPathSink(owner);
        return event -> {
            if (event.type() == atf.api.reporting.console.AtfConsoleEvent.Type.REPORT_PUBLISHED) {
                coreSink.publish(atf.api.core.reporting.console.AtfConsoleEvent.reportPublished(
                        Path.of(event.detailValue())));
            }
        };
    }
}
