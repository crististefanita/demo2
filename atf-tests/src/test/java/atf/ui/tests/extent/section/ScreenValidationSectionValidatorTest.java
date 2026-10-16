package atf.ui.tests.extent.section;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.section.validate.ScreenValidationSectionValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenValidationSectionValidatorTest {
    @Test
    void acceptsExactlyThreeContiguousZonesAndCanonicalInlineImage() {
        var snapshot = new ReportAuditSnapshot.ScreenValidation(1, 1, 1, 1, 3,
                1, 1, true, true, true, true, true, List.of());
        assertTrue(new ScreenValidationSectionValidator().validate(snapshot).valid());
    }

    @Test
    void rejectsRepeatedOrSeparatedEvidenceZones() {
        var snapshot = new ReportAuditSnapshot.ScreenValidation(1, 2, 1, 1, 4,
                1, 1, false, true, false, false, true,
                List.of("unexpected separator"));
        assertFalse(new ScreenValidationSectionValidator().validate(snapshot).valid());
    }
}
