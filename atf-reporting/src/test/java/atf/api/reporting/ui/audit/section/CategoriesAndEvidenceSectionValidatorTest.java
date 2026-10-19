package atf.api.reporting.ui.audit.section;
import atf.api.testkit.AtfUnitTest;

import atf.api.reporting.audit.ReportAuditSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@AtfUnitTest
class CategoriesAndEvidenceSectionValidatorTest {
    @Test
    void validatesDynamicCategoryCardinalityWithoutHardCodedRows() {
        var snapshot = new ReportAuditSnapshot.Annotations(3, 4,
                "#010203 / #040506", true, List.of(0, 1, 3));
        assertTrue(new CategoriesSectionValidator().validate(snapshot).valid());
    }

    @Test
    void rejectsNonHexComputedColorEvidence() {
        String browserNormalized = "rgb" + "(16, 23, 35) / rgb" + "(229, 236, 247)";
        var snapshot = new ReportAuditSnapshot.Annotations(3, 4,
                browserNormalized, true, List.of(0, 1, 3));
        assertFalse(new CategoriesSectionValidator().validate(snapshot).valid());
    }

    @Test
    void validatesRequestResponseAndExecutionJsonAsOneEvidenceContract() {
        var json = new ReportAuditSnapshot.JsonEvidence("{\"id\":1}",
                "{\"id\":1}", List.of());
        var snapshot = new ReportAuditSnapshot.RequestEvidence(true, 1, 1, 1, json);
        assertTrue(new EvidenceSectionValidator().validate(snapshot).valid());
    }
}
