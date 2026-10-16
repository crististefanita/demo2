package atf.ui.tests.extent.section;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.validate.CategoriesSectionValidator;
import atf.ui.tests.extent.section.validate.EvidenceSectionValidator;
import atf.ui.tests.extent.section.validate.JsonEvidenceValidator;
import atf.ui.tests.extent.section.validate.VisualEvidenceValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Locks the extractor -> snapshot -> pure validator -> ValidationResult seam. */
class ExtractionValidationConvergenceTest {
    @Test
    void categoryAndVisualSnapshotsAreValidatedWithoutBrowserObjects() {
        var categories = new ReportAuditSnapshot.Annotations(3, 4,
                "rgb(1, 2, 3) / rgb(4, 5, 6)", true, List.of(0, 1, 3));
        var visual = new ReportAuditSnapshot.VisualEvidence(1, 3, 1, 1,
                0, 0, 1, 1, 1, 1, "Navigate | target", true, true);

        assertTrue(new CategoriesSectionValidator().validate(categories).valid());
        assertTrue(new VisualEvidenceValidator().validate(visual).valid());
    }

    @Test
    void requestEvidenceUsesOnePureJsonContract() {
        var json = new ReportAuditSnapshot.JsonEvidence("{\"id\":1}",
                "{\"id\":1}", List.of());
        var request = new ReportAuditSnapshot.RequestEvidence(true, 1, 1, 1, json);

        assertTrue(new JsonEvidenceValidator().validate(json).valid());
        assertTrue(new EvidenceSectionValidator().validate(request).valid());
    }

    @Test
    void malformedJsonFailsOnlyThePureValidator() {
        var json = new ReportAuditSnapshot.JsonEvidence("{invalid", "{\"id\":1}", List.of());

        assertFalse(new JsonEvidenceValidator().validate(json).valid());
    }
}
