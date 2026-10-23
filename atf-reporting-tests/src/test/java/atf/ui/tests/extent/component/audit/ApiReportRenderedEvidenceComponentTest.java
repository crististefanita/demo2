package atf.ui.tests.extent.component.audit;

import atf.api.testkit.AtfComponentTest;
import atf.ui.tests.extent.support.inspection.reconciliation.ApiReportRenderedEvidenceProbe;
import org.junit.jupiter.api.Test;

/** Controlled publication and real runtime, without an application endpoint. */
@AtfComponentTest
class ApiReportRenderedEvidenceComponentTest {
    @Test
    void reconcilesCanonicalRenderedMetadataAndQuery() {
        ApiReportRenderedEvidenceProbe.verifyMetadata(false);
    }

    @Test
    void responseUrlCannotHideMissingQueryInTheRequestTable() {
        ApiReportRenderedEvidenceProbe.verifyMetadata(true);
    }

    @Test
    void phaseEvidenceSurvivesReselectionAndTabRoundTrips() {
        ApiReportRenderedEvidenceProbe.verifyPhaseNavigation();
    }

    @Test
    void absentTestBodyCannotBecomePassingEvidence() {
        ApiReportRenderedEvidenceProbe.verifyMissingPhase();
    }
}
