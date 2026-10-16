package atf.ui.tests.extent.flows.failure;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.reporting.UiExtentReportSession;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.tests.extent.support.ReportAuditContext;
import atf.ui.tests.extent.support.UiAuditLifecycle;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static atf.ui.tests.extent.support.ReportAuditChecks.check;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Controlled failure-path audit. The JUnit method passes because the failure
 * is intentional, while the generated Extent audit contains a real FAIL
 * scenario with the executed actions and the YAML-controlled screenshot.
 * This package is deliberately separate from happy-path exploratory flows.
 */
@EnabledIf("isConfigured")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Extent failure evidence flow")
class FailureEvidenceFlowTest {
    private UiConfiguration configuration;
    private Path inputReport;
    private Path auditReport;
    private ReportAuditContext auditContext;
    private PlaywrightActionSequence actions;

    static boolean isConfigured() {
        UiConfiguration loaded = AtfUiConfigurationLoader.load();
        return loaded.auditInputReport().isPresent();
    }

    @BeforeAll
    void openConfiguredReport() {
        configuration = AtfUiConfigurationLoader.load();
        inputReport = configuration.auditInputReport().orElseThrow();
        assertTrue(Files.isRegularFile(inputReport),
                "Configured Extent report does not exist: " + inputReport);
        auditContext = ReportAuditContext.open(configuration, inputReport, "failure-flow");
        auditReport = auditContext.reportPath();
    }

    @AfterAll
    void closeAudit() {
        if (auditContext != null) auditContext.close();
    }

    @Test
    @DisplayName("GIVEN a controlled UI failure WHEN the failure boundary closes THEN actions and policy screenshot are auditable")
    void failureEvidenceUsesConfiguredScreenshotPolicy() throws Exception {
        actions = auditContext.newActionSequence();
        UiExtentReportSession.UiScenario scenario = auditContext.startScenario(
                "FailureEvidenceFlowTest", "atf.ui.tests.extent.flows.failure",
                "Controlled failure evidence", List.of("ui", "failure", "diagnostic"));
        UiExtentReportSession.UiPhase setup = scenario.startPhase("SETUP");
        setup.step("GIVEN", "the configured Extent report is loaded read-only",
                inputReport.getFileName().toString(), null,
                List.of(check("Input report", "published HTML", inputReport.toString(),
                        Files.isRegularFile(inputReport))), AtfReportStatus.PASS);
        setup.complete(AtfReportStatus.PASS);

        UiExtentReportSession.UiPhase body = scenario.startPhase("TEST BODY");
        UiAuditLifecycle lifecycle = new UiAuditLifecycle(scenario, body);
        Page page = auditContext.openTab("#test-view", actions);
        actions.observation("Failure-path probe", "#test-view",
                "a controlled failure is captured at the current report state", "intentional failure");
        UiScreenValidation.Evidence screenshot = auditContext.captureFailure(
                configuration, "Controlled UI failure");
        boolean capturedFromTestsTab = page.url().contains("#test-view");
        lifecycle.fail("Controlled UI failure", "#test-view", actions.drain(),
                new AssertionError("intentional failure for failure-evidence contract"),
                screenshot.hasScreenshotPayload() ? screenshot.base64Png() : null);

        auditContext.close();
        auditContext = null;
        String html = Files.readString(auditReport);
        boolean failureRendered = html.contains("Controlled UI failure")
                && html.contains("atf-ui-action-result-fail");
        boolean screenshotPolicyRendered = configuration.browser().screenshotOnFailure()
                ? screenshot.hasScreenshotPayload() && html.contains("data:image/png;base64,")
                : !screenshot.hasScreenshotPayload();
        assertTrue(failureRendered, "The controlled FAIL scenario is not present in the audit artifact");
        assertTrue(screenshotPolicyRendered,
                "The failure screenshot does not follow ui.screenshotOnFailure");
        assertTrue(capturedFromTestsTab, "Failure evidence was captured from the wrong tab");
    }
}


