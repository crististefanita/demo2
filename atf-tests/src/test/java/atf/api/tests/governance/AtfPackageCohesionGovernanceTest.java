package atf.api.tests.governance;
import atf.api.core.config.module.AtfModuleIds;
import atf.api.testkit.AtfComponentTest;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static atf.api.tests.governance.AtfGovernanceTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards package size and keeps cohesion exceptions explicit. */
@AtfComponentTest
public class AtfPackageCohesionGovernanceTest {
    @Test
    void productionPackageCohesionUsesAnExplicitReviewedExceptionList() throws Exception {
        Path root = locateProjectRoot();
        Map<String, Integer> counts = productionPackageCounts(root);
        Map<String, List<String>> inventory = productionPackageClassInventory(root);
        Map<String, Integer> overLimit = new TreeMap<>();
        counts.forEach((packageName, count) -> {
            if (count > PACKAGE_COHESION_LIMIT) overLimit.put(packageName, count);
        });
        Map<String, Integer> unreviewed = withoutReviewedEntries(overLimit, REVIEWED_COHESION_EXCEPTIONS);

        assertTrue(!counts.isEmpty(), "Production package inventory must not be empty");
        assertTrue(unreviewed.isEmpty(),
                () -> "New package cohesion hotspot requires an explicit review: "
                        + describe(unreviewed, inventory));
        assertTrue(REVIEWED_COHESION_EXCEPTIONS.keySet().stream()
                        .filter(counts::containsKey)
                        .allMatch(overLimit::containsKey),
                () -> "Reviewed package allow-list is stale: " + overLimit);
    }

    @Test
    void aiValidationUsesSemanticSubpackagesWithoutAnAllowListException() throws Exception {
        Map<String, List<String>> inventory = productionPackageClassInventory(locateProjectRoot());

        assertEquals(Set.of(
                        "AiEvaluationCase.java",
                        "AiEvaluationEvaluator.java",
                        "AiEvaluationPolicy.java",
                        "AiEvaluationSnapshot.java",
                        "AiEvaluationValues.java",
                        "AiFieldComparison.java",
                        "AiFieldExpectation.java"),
                Set.copyOf(inventory.getOrDefault("atf.api.validation.ai", List.of())));
        assertEquals(Set.of("AiGroundingObservation.java", "AiGroundingRequirement.java",
                        "AiSourceReference.java"),
                Set.copyOf(inventory.getOrDefault("atf.api.validation.ai.grounding", List.of())));
        assertEquals(List.of("AiInvocationEvidence.java"),
                inventory.getOrDefault("atf.api.validation.ai.invocation", List.of()));
        assertFalse(inventory.containsKey("atf.api.validation.ai.support"),
                "internal AI defensive-copy helpers must not become a public support package");
    }

    @Test
    void testKitPackagesKeepCohesionBoundaries() throws Exception {
        Path tests = locateProjectRoot().resolve(AtfModuleIds.REPORTING_TESTS)
                .resolve("src/test/java");
        Map<String, Integer> counts = testPackageCounts(tests);
        Map<String, List<String>> inventory = packageClassInventory(tests);
        Map<String, Integer> overLimit = new TreeMap<>();
        counts.forEach((packageName, count) -> {
            if (count > PACKAGE_COHESION_LIMIT) overLimit.put(packageName, count);
        });

        assertTrue(overLimit.isEmpty(), () ->
                "Test-kit package exceeds the eight-class cohesion limit: "
                        + describe(overLimit, inventory));
        assertFalse(counts.containsKey("atf.ui.tests.extent.support.section"),
                "section root must not become a mixed test package again");
        assertTrue(counts.getOrDefault("atf.ui.tests.extent.e2e.section", 0) <= 6);
        assertTrue(counts.getOrDefault("atf.ui.tests.extent.component.section.contract", 0) <= 6);
        assertTrue(counts.getOrDefault("atf.ui.tests.extent.support.section.validation", 0) <= 6);
        assertTrue(counts.getOrDefault("atf.ui.tests.extent.component.section.architecture", 0) <= 6);
    }

    @Test
    void allTestSourceSetsRespectTheEightClassCohesionLimitUnlessReviewed() throws Exception {
        Path root = locateProjectRoot();
        Map<String, Integer> overLimit = new TreeMap<>();
        Map<String, List<String>> inventory = new TreeMap<>();
        for (String module : PRODUCTION_MODULES) {
            for (String sourceDirectory : java.util.List.of("src/test/java", "src/test")) {
                Path sourceRoot = root.resolve(module).resolve(sourceDirectory);
                if (!java.nio.file.Files.isDirectory(sourceRoot)) continue;
                testPackageCounts(sourceRoot).forEach((packageName, count) -> {
                    if (count > PACKAGE_COHESION_LIMIT) overLimit.put(module + ":" + packageName, count);
                });
                packageClassInventory(sourceRoot).forEach((packageName, classes) ->
                        inventory.put(module + ":" + packageName, classes));
            }
        }
        Map<String, Integer> unreviewed = withoutReviewedEntries(overLimit, REVIEWED_TEST_COHESION_EXCEPTIONS);
        assertTrue(unreviewed.isEmpty(),
                () -> "New test-package cohesion hotspot requires an explicit review: "
                        + describe(unreviewed, inventory));
        assertTrue(REVIEWED_TEST_COHESION_EXCEPTIONS.keySet().stream()
                        .allMatch(overLimit::containsKey),
                () -> "Reviewed test-package allow-list is stale: " + overLimit);
    }

    private static Map<String, Integer> withoutReviewedEntries(Map<String, Integer> counts,
                                                                 Map<String, String> reviewed) {
        Map<String, Integer> unreviewed = new TreeMap<>(counts);
        unreviewed.keySet().removeAll(reviewed.keySet());
        return unreviewed;
    }

    private static String describe(Map<String, Integer> counts,
                                   Map<String, List<String>> inventory) {
        return counts.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue()
                        + " [" + String.join(", ", inventory.getOrDefault(entry.getKey(), List.of())) + "]")
                .toList()
                .toString();
    }
}
