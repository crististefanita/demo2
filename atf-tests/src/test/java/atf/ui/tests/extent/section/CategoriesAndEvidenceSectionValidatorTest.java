package atf.ui.tests.extent.section;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.validate.CategoriesSectionValidator;
import atf.ui.tests.extent.section.validate.EvidenceSectionValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoriesAndEvidenceSectionValidatorTest {
    @Test
    void validatesDynamicCategoryCardinalityWithoutHardCodedRows() {
        var snapshot = new ReportAuditSnapshot.Annotations(3, 4,
                "rgb(1, 2, 3) / rgb(4, 5, 6)", true, List.of(0, 1, 3));
        assertTrue(new CategoriesSectionValidator().validate(snapshot).valid());
    }

    @Test
    void validatesRequestResponseAndExecutionJsonAsOneEvidenceContract() {
        var json = new ReportAuditSnapshot.JsonEvidence("{\"id\":1}",
                "{\"id\":1}", List.of());
        var snapshot = new ReportAuditSnapshot.RequestEvidence(true, 1, 1, 1, json);
        assertTrue(new EvidenceSectionValidator().validate(snapshot).valid());
    }
}
