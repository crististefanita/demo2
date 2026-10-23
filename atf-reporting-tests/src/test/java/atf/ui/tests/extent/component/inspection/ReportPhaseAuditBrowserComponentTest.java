package atf.ui.tests.extent.component.inspection;

import atf.api.testkit.AtfComponentTest;
import atf.ui.tests.extent.support.inspection.diagnostic.ReportPhaseAuditProbe;
import org.junit.jupiter.api.Test;

@AtfComponentTest
class ReportPhaseAuditBrowserComponentTest {
    @Test
    void hidingRenamingOrEmptyingThePhaseLogBandDoesNotSatisfyTheContract() {
        ReportPhaseAuditProbe.rejectsHiddenRenamedAndEmptyPhaseLogBands();
    }

    @Test
    void extentKeepsPhaseMetadataInternalAndPublishesActualFailureAtTheScenario() throws Exception {
        ReportPhaseAuditProbe.verify(false);
    }

    @Test
    void fallbackKeepsTheSameEvidenceOwnershipAndFailureDiagnostic() throws Exception {
        ReportPhaseAuditProbe.verify(true);
    }
}
