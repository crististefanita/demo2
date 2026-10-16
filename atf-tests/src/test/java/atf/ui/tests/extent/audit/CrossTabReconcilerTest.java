package atf.ui.tests.extent.audit;

import atf.ui.tests.extent.support.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrossTabReconcilerTest {
    private final CrossTabReconciler reconciler = new CrossTabReconciler();

    @Test
    void acceptsEquivalentOwnerSetsAndProjectedCounts() {
        ValidationResult result = reconciler.reconcile(
                List.of("UsersApiTests", "PostsApiTests"),
                List.of("PostsApiTests", "UsersApiTests"), 2, 4);

        assertTrue(result.valid(), result::failureSummary);
        assertTrue(result.violations().isEmpty());
    }

    @Test
    void reportsEveryProjectionDrift() {
        ValidationResult result = reconciler.reconcile(
                List.of("UsersApiTests", "PostsApiTests"),
                List.of("UsersApiTests", "UnexpectedApiTests"), 3, 0);

        assertFalse(result.valid());
        assertTrue(result.violations().stream().anyMatch(value -> value.startsWith("Owners:")));
        assertTrue(result.violations().stream().anyMatch(value -> value.startsWith("Overview classes:")));
        assertTrue(result.violations().stream().anyMatch(value -> value.startsWith("Category links:")));
    }
}
