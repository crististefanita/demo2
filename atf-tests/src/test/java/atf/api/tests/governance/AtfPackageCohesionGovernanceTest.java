package atf.api.tests.governance;
import atf.api.core.config.module.AtfModuleIds;
import atf.api.testkit.AtfComponentTest;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import static atf.api.tests.governance.AtfGovernanceTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards package size and keeps cohesion exceptions explicit. */
@AtfComponentTest
public class AtfPackageCohesionGovernanceTest {
    @Test
    void productionPackageCohesionUsesAnExplicitReviewedExceptionList() throws Exception {
        Path root = locateProjectRoot();
        Map<String, Integer> counts = productionPackageCounts(root);
        Map<String, Integer> overLimit = new TreeMap<>();
        counts.forEach((packageName, count) -> {
            if (count > PACKAGE_COHESION_LIMIT) overLimit.put(packageName, count);
        });

        assertTrue(!counts.isEmpty(), "Production package inventory must not be empty");
        assertTrue(overLimit.keySet().stream().allMatch(REVIEWED_COHESION_EXCEPTIONS::containsKey),
                () -> "New package cohesion hotspot requires an explicit review: " + overLimit);
        assertTrue(REVIEWED_COHESION_EXCEPTIONS.keySet().stream()
                        .filter(counts::containsKey)
                        .allMatch(overLimit::containsKey),
                () -> "Reviewed package allow-list is stale: " + overLimit);
    }

    @Test
    void testKitPackagesKeepCohesionBoundaries() throws Exception {
        Path tests = locateProjectRoot().resolve(AtfModuleIds.REPORTING_TESTS)
                .resolve("src/test/java");
        Map<String, Integer> counts = testPackageCounts(tests);
        Map<String, Integer> overLimit = new TreeMap<>();
        counts.forEach((packageName, count) -> {
            if (count > PACKAGE_COHESION_LIMIT) overLimit.put(packageName, count);
        });

        assertTrue(overLimit.isEmpty(), () ->
                "Test-kit package exceeds the eight-class cohesion limit: " + overLimit);
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
        for (String module : PRODUCTION_MODULES) {
            for (String sourceDirectory : java.util.List.of("src/test/java", "src/test")) {
                Path sourceRoot = root.resolve(module).resolve(sourceDirectory);
                if (!java.nio.file.Files.isDirectory(sourceRoot)) continue;
                testPackageCounts(sourceRoot).forEach((packageName, count) -> {
                    if (count > PACKAGE_COHESION_LIMIT) overLimit.put(module + ":" + packageName, count);
                });
            }
        }
        assertTrue(overLimit.keySet().stream().allMatch(REVIEWED_TEST_COHESION_EXCEPTIONS::containsKey),
                () -> "New test-package cohesion hotspot requires an explicit review: " + overLimit);
        assertTrue(REVIEWED_TEST_COHESION_EXCEPTIONS.keySet().stream()
                        .allMatch(overLimit::containsKey),
                () -> "Reviewed test-package allow-list is stale: " + overLimit);
    }
}
