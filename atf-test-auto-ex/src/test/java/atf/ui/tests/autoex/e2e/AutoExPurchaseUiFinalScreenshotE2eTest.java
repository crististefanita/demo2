package atf.ui.tests.autoex.e2e;

import atf.api.console.junit.AtfConsoleTestExtension;
import atf.api.testkit.AtfConsoleMessages;
import atf.api.testkit.AtfE2eTest;
import atf.api.testkit.reporting.AtfReportPath;
import atf.api.testkit.evidence.AtfUiEvidence;
import atf.api.testkit.evidence.AtfUiEvidenceFlow;
import atf.api.testkit.evidence.AtfUiScreenshotPolicy;
import atf.api.testkit.channel.AtfTestChannel;
import atf.api.testkit.channel.TestChannel;
import atf.api.testkit.output.UiReportSubdirectory;
import atf.ui.autoex.support.AutoExE2eExtension;
import atf.ui.autoex.support.AutoExE2eRun;
import atf.ui.autoex.support.journey.AutoExPurchaseJourney;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/** Same UI-only journey, one consolidated Playwright table, and one final screenshot. */
@AtfE2eTest
@AtfTestChannel(TestChannel.UI)
@AtfUiEvidence(flow = AtfUiEvidenceFlow.UNIFIED_FLOW,
        screenshots = AtfUiScreenshotPolicy.FINAL_ONLY)
@UiReportSubdirectory("auto-ex")
@AtfConsoleMessages(AtfConsoleMessages.Policy.DETAILS)
@AtfReportPath
@ExtendWith({AtfConsoleTestExtension.class, AutoExE2eExtension.class})
@Execution(ExecutionMode.SAME_THREAD)
@Order(3)
@DisplayName("Automation Exercise purchase — UI, final screenshot only")
public class AutoExPurchaseUiFinalScreenshotE2eTest {
    @Test
    @DisplayName("customer completes the same demo purchase with a single terminal screenshot")
    void completesPurchaseWithOneFinalScreenshot(AutoExE2eRun run) {
        AutoExPurchaseJourney.execute(run);
    }
}
