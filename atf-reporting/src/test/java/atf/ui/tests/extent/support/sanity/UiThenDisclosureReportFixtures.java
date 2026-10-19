package atf.ui.tests.extent.support.sanity;
import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.core.reporting.contract.AtfReportStatus;
import atf.api.reporting.ui.UiExtentReportSession;
import atf.api.reporting.ui.contract.ReportUiContract;


import java.nio.file.Path;
import java.util.List;

import static atf.api.core.reporting.contract.AtfGherkinKeyword.THEN;

/** Standalone Extent inputs for the THEN disclosure sanity tests. */
public final class UiThenDisclosureReportFixtures {
    public static final String THEN_TITLE =
            "THEN the existing report audit completed and published a separate UI artifact";

    private UiThenDisclosureReportFixtures() {
    }

    public static Path publishTeardown(Path root) {
        UiExtentReportSession session = UiExtentReportSession.openConfigured(
                root, "then-ui-disclosure-sanity.html", AtfConsoleSink.noOp());
        Path report = session.reportPath();
        try (session) {
            var scenario = session.startScenario("UiThenDisclosureSanityTest",
                    "atf.ui.tests.extent.e2e.sanity", "Existing report audit", List.of("ui", "sanity"));
            var teardown = scenario.startPhase(ReportUiContract.Phase.TEARDOWN.displayName());
            teardown.outcome(THEN, THEN_TITLE,
                    "UI audit report", AtfReportStatus.PASS);
            teardown.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
        return report;
    }

    public static Path publishWithoutTeardown(Path root) {
        UiExtentReportSession session = UiExtentReportSession.openConfigured(
                root, "ui-no-teardown-sanity.html", AtfConsoleSink.noOp());
        Path report = session.reportPath();
        try (session) {
            var scenario = session.startScenario("UiThenDisclosureSanityTest",
                    "atf.ui.tests.extent.e2e.sanity", "UI audit without teardown", List.of("ui", "sanity"));
            var setup = scenario.startPhase(ReportUiContract.Phase.SETUP.displayName());
            setup.complete(AtfReportStatus.PASS);
            var body = scenario.startPhase(ReportUiContract.Phase.TEST_BODY.displayName());
            body.step(THEN, "the UI audit is rendered",
                    ReportUiContract.Tab.TESTS.hash(), null, AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
        return report;
    }
}
