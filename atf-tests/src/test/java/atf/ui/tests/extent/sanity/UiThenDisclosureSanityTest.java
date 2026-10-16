package atf.ui.tests.extent.sanity;

import atf.api.core.reporting.AtfConsoleSink;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.debugui.DebugUiConfiguration;
import atf.api.debugui.DebugUiServer;
import atf.api.reporting.UiExtentReportSession;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.playwright.PlaywrightUiSession;
import atf.ui.tests.extent.flows.ThenDisclosureFlow;
import atf.ui.tests.extent.support.ReportUiContract;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Intent/assertions only; the DOM contract is owned by {@link ThenDisclosureFlow}. */
class UiThenDisclosureSanityTest {
    private static final String THEN_TITLE =
            "THEN the existing report audit completed and published a separate UI artifact";

    @Test
    void explicitTeardownOutcomeIsStandaloneGherkinDisclosure() throws Exception {
        UiConfiguration configuration = AtfUiConfigurationLoader.load();
        Path root = Files.createTempDirectory("atf-ui-then-disclosure-");
        Path report = publishFixture(root);
        try (DebugUiServer server = DebugUiServer.start(DebugUiConfiguration.forHtml(report, 0));
             PlaywrightUiSession browser = PlaywrightUiSession.open(configuration.browser())) {
            browser.navigate(java.net.URI.create(server.uriFor(report) + ReportUiContract.Tab.TESTS.hash()));
            ThenDisclosureFlow.ThenSnapshot snapshot = new ThenDisclosureFlow().inspectThenDisclosure(browser.page());
            assertAll(
                    () -> assertTrue(snapshot.rendered(), "The THEN outcome must be rendered"),
                    () -> assertEquals("request", snapshot.role()),
                    () -> assertTrue(snapshot.initialized(), "The THEN card must be initialized as a disclosure"),
                    () -> assertTrue(snapshot.disclosureGlyph(), "The collapsed THEN card must expose a disclosure glyph"),
                    () -> assertEquals(0, snapshot.nestedUiValidations(), "The publication outcome must not contain a synthetic UI validation child"),
                    () -> assertTrue(snapshot.titleVisible(), "The final Gherkin outcome must remain visible"));
        }
    }

    @Test
    void uiAuditWithoutTeardownRendersOnlyRequestedPhases() throws Exception {
        UiConfiguration configuration = AtfUiConfigurationLoader.load();
        Path root = Files.createTempDirectory("atf-ui-no-teardown-sanity-");
        Path report = publishAuditWithoutTeardownFixture(root);
        try (DebugUiServer server = DebugUiServer.start(DebugUiConfiguration.forHtml(report, 0));
             PlaywrightUiSession browser = PlaywrightUiSession.open(configuration.browser())) {
            browser.navigate(java.net.URI.create(server.uriFor(report) + ReportUiContract.Tab.TESTS.hash()));
            ThenDisclosureFlow.PhaseSnapshot snapshot = new ThenDisclosureFlow().inspectWithoutTeardown(browser.page());
            assertAll(
                    () -> assertEquals(2, snapshot.phaseCount(), "Only SETUP and TEST BODY should render"),
                    () -> assertTrue(snapshot.setupVisible()),
                    () -> assertTrue(snapshot.testBodyVisible()),
                    () -> assertTrue(!snapshot.teardownVisible(), "A UI audit without cleanup must not render TEARDOWN"));
        }
    }

    private static Path publishFixture(Path root) {
        UiExtentReportSession session = UiExtentReportSession.openConfigured(root, "then-ui-disclosure-sanity.html", AtfConsoleSink.noOp());
        Path report = session.reportPath();
        try (session) {
            var scenario = session.startScenario("UiThenDisclosureSanityTest", "atf.ui.tests.extent.sanity", "Existing report audit", List.of("ui", "sanity"));
            var teardown = scenario.startPhase(ReportUiContract.Phase.TEARDOWN.displayName());
            teardown.outcome("THEN", THEN_TITLE, "UI audit report", AtfReportStatus.PASS);
            teardown.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
        return report;
    }

    private static Path publishAuditWithoutTeardownFixture(Path root) {
        UiExtentReportSession session = UiExtentReportSession.openConfigured(root, "ui-no-teardown-sanity.html", AtfConsoleSink.noOp());
        Path report = session.reportPath();
        try (session) {
            var scenario = session.startScenario("UiThenDisclosureSanityTest", "atf.ui.tests.extent.sanity", "UI audit without teardown", List.of("ui", "sanity"));
            var setup = scenario.startPhase(ReportUiContract.Phase.SETUP.displayName());
            setup.complete(AtfReportStatus.PASS);
            var body = scenario.startPhase(ReportUiContract.Phase.TEST_BODY.displayName());
            body.step("THEN", "the UI audit is rendered", ReportUiContract.Tab.TESTS.hash(), null, AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
        return report;
    }
}
