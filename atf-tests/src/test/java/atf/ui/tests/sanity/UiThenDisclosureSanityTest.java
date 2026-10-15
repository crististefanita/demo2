package atf.ui.tests.sanity;

import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfConsoleSink;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.debugui.DebugUiConfiguration;
import atf.api.debugui.DebugUiServer;
import atf.api.reporting.UiExtentReportSession;
import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.playwright.PlaywrightUiSession;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Browser-level regression for the visual contract of optional UI lifecycle
 * phases. It keeps one explicit teardown fixture because the reporting API
 * must support teardown when a UI test has real cleanup or publication work.
 */
@EnabledIf("isConfigured")
class UiThenDisclosureSanityTest {
    private static final String THEN_TITLE =
            "THEN the existing report audit completed and published a separate UI artifact";

    static boolean isConfigured() {
        return Boolean.parseBoolean(System.getProperty("atf.ui.enabled", "false"));
    }

    @Test
    void explicitTeardownOutcomeIsStandaloneGherkinDisclosure() throws Exception {
        UiConfiguration configuration = AtfUiConfigurationLoader.load();
        Path root = Files.createTempDirectory("atf-ui-then-disclosure-");
        Path report = publishFixture(root);

        try (DebugUiServer server = DebugUiServer.start(DebugUiConfiguration.forHtml(report, 0));
             PlaywrightUiSession browser = PlaywrightUiSession.open(configuration.browser())) {
            browser.navigate(java.net.URI.create(server.uriFor(report) + "#test-view"));
            Page page = browser.page();
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            page.locator("#test-view").waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE));

            Locator owner = page.locator("#test-view .test-list .test-item").first();
            owner.click();
            Locator detail = page.locator("#test-view .test-content-detail:visible").last();
            detail.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.waitForTimeout(100);

            Locator thenNode = detail.locator(".card-title .node")
                    .filter(new Locator.FilterOptions().setHasText(THEN_TITLE)).first();
            Locator thenCard = cardOf(thenNode);
            Locator thenHeader = thenCard.locator(":scope > .card-header");
            String thenStyle = headerStyle(thenHeader);
            String renderedText = detail.textContent();
            int nestedUiValidations = thenCard.locator(".card-title .node")
                    .filter(new Locator.FilterOptions().setHasText("UI validation")).count();

            assertAll(
                    () -> assertTrue(thenNode.count() > 0, "The THEN outcome must be rendered"),
                    () -> assertEquals("request", thenCard.getAttribute("data-atf-node-role")),
                    () -> assertTrue(thenCard.getAttribute("data-atf-node-open") != null,
                            "The THEN card must be initialized as a disclosure"),
                    () -> assertTrue(thenStyle.endsWith("|true"),
                            "The collapsed THEN card must expose a disclosure glyph"),
                    () -> assertEquals(0, nestedUiValidations,
                            "The publication outcome must not contain a synthetic UI validation child"),
                    () -> assertTrue(renderedText.contains(THEN_TITLE),
                            "The final Gherkin outcome must remain visible"));
        }
    }

    @Test
    void uiAuditWithoutTeardownRendersOnlyRequestedPhases() throws Exception {
        UiConfiguration configuration = AtfUiConfigurationLoader.load();
        Path root = Files.createTempDirectory("atf-ui-no-teardown-sanity-");
        Path report = publishAuditWithoutTeardownFixture(root);

        try (DebugUiServer server = DebugUiServer.start(DebugUiConfiguration.forHtml(report, 0));
             PlaywrightUiSession browser = PlaywrightUiSession.open(configuration.browser())) {
            browser.navigate(java.net.URI.create(server.uriFor(report) + "#test-view"));
            Page page = browser.page();
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            page.locator("#test-view").waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE));

            Locator owner = page.locator("#test-view .test-list .test-item").first();
            owner.click();
            Locator detail = page.locator("#test-view .test-content-detail:visible").last();
            detail.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.waitForTimeout(100);

            Locator phases = detail.locator("[data-atf-node-role='phase']");
            String renderedText = detail.textContent();
            assertAll(
                    () -> assertEquals(2, phases.count(),
                            "Only the explicitly requested SETUP and TEST BODY phases should render"),
                    () -> assertTrue(renderedText.contains("SETUP")),
                    () -> assertTrue(renderedText.contains("TEST BODY")),
                    () -> assertTrue(!renderedText.contains("TEARDOWN"),
                            "A UI audit without cleanup must not render TEARDOWN"));
        }
    }

    private static Path publishFixture(Path root) {
        UiExtentReportSession session = UiExtentReportSession.openConfigured(
                root, "then-ui-disclosure-sanity.html", AtfConsoleSink.noOp());
        Path report = session.reportPath();
        try (session) {
            var scenario = session.startScenario("UiThenDisclosureSanityTest",
                    "atf.ui.tests.sanity", "Existing report audit", List.of("ui", "sanity"));
            var teardown = scenario.startPhase("TEARDOWN");
            teardown.outcome("THEN", THEN_TITLE, "UI audit report", AtfReportStatus.PASS);
            teardown.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
        return report;
    }

    private static Path publishAuditWithoutTeardownFixture(Path root) {
        UiExtentReportSession session = UiExtentReportSession.openConfigured(
                root, "ui-no-teardown-sanity.html", AtfConsoleSink.noOp());
        Path report = session.reportPath();
        try (session) {
            var scenario = session.startScenario("UiThenDisclosureSanityTest",
                    "atf.ui.tests.sanity", "UI audit without teardown", List.of("ui", "sanity"));
            var setup = scenario.startPhase("SETUP");
            setup.complete(AtfReportStatus.PASS);
            var body = scenario.startPhase("TEST BODY");
            body.step("THEN", "the UI audit is rendered", "#test-view", null,
                    AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
        return report;
    }

    private static Locator cardOf(Locator node) {
        return node.locator("xpath=ancestor::div[contains(concat(' ', normalize-space(@class), ' '), ' card ')][1]");
    }

    private static String headerStyle(Locator header) {
        if (header.count() == 0) return "missing";
        return header.evaluate("element => { const s=getComputedStyle(element), p=getComputedStyle(element, '::before'); "
                + "return [s.backgroundColor,s.borderColor,s.borderRadius,"
                + "p.content !== 'none' && p.content !== 'normal' && p.content !== ''].join('|'); }")
                .toString();
    }
}
