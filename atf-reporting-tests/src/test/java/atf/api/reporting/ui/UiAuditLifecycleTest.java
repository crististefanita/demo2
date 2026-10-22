package atf.api.reporting.ui;
import atf.api.testkit.AtfUnitTest;

import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.ui.model.AtfUiActionRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class UiAuditLifecycleTest {
    private static final String ONE_PIXEL_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";

    @Test
    void publishesExecutedActionsBeforeClosingTheScenarioOnFailure(@TempDir Path root) throws Exception {
        Path report;
        try (var session = UiExtentReportSession.openConfigured(
                root, "failure-boundary.html", AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiAuditLifecycleTest",
                    "atf.api.reporting.ui", "failure boundary", List.of("ui", "failure"));
            var body = scenario.startPhase("TEST BODY");
            new UiAuditLifecycle(scenario, body).fail(
                    "UI exploration failed", "#test-view",
                    List.of(new AtfUiActionRecord(1, "Click", "#test-view",
                            "Tests is visible", "click completed", 7, AtfReportStatus.PASS)),
                    new IllegalStateException("synthetic failure"), ONE_PIXEL_PNG);
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("ui-failure")),
                () -> assertTrue(html.contains("Click") && html.contains("#test-view")),
                () -> assertTrue(html.contains("Failure cause") && html.contains("synthetic failure")),
                () -> assertTrue(html.contains("UI failure screenshot")),
                () -> assertTrue(html.contains("data:image/png;base64,")),
                () -> assertTrue(html.contains("status=\"fail\"")));
    }

    @Test
    void preservesActionsWhenFailureHasNoScreenshot(@TempDir Path root) throws Exception {
        Path report;
        try (var session = UiExtentReportSession.openConfigured(
                root, "failure-without-screenshot.html", AtfConsoleSink.noOp())) {
            report = session.reportPath();
            var scenario = session.startScenario("UiAuditLifecycleTest",
                    "atf.api.reporting.ui", "failure without screenshot", List.of("ui", "failure"));
            var body = scenario.startPhase("TEST BODY");
            new UiAuditLifecycle(scenario, body).fail(
                    "UI exploration failed", "#test-view",
                    List.of(new AtfUiActionRecord(1, "Click", "#test-view",
                            "Tests is visible", "click completed", 7, AtfReportStatus.PASS)),
                    new IllegalStateException("capture unavailable"));
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertTrue(html.contains("capture unavailable")),
                () -> assertTrue(html.contains("Click") && html.contains("#test-view")),
                () -> assertTrue(html.contains("screenshot disabled or unavailable")),
                () -> assertTrue(!html.contains("data-atf-screenshot=\"base64\"")),
                () -> assertTrue(html.contains("status=\"fail\"")));
    }
}
