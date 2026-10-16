package atf.ui.tests.extent.audit;

import atf.ui.tests.extent.support.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the common adapter contract used by non-cross-tab auditors. */
class ValidationResultConvergenceTest {
    @Test
    void legacyFindingListsBecomeAuditableAssertionsAndMergeWithoutLoss() {
        ValidationResult json = ValidationResult.fromViolations("JSON", List.of("request missing"));
        ValidationResult style = ValidationResult.fromViolations("Style", List.of());

        ValidationResult merged = json.merge(style);

        assertFalse(merged.valid());
        assertTrue(merged.violations().contains("request missing"));
        assertTrue(merged.assertions().stream().anyMatch(assertion -> assertion.name().startsWith("JSON")));
    }
}
