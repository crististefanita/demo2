package atf.api.reporting.governance;

import atf.api.testkit.AtfComponentTest;
import atf.api.testkit.AtfE2eTest;
import atf.api.testkit.AtfIntegrationTest;
import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keeps the reporting seam-to-test-level matrix executable and reviewable. */
@AtfComponentTest
class ReportingCoverageMatrixTest {
    private static final List<Entry> MATRIX = List.of(
            entry("execution projections", "atf.api.reporting.model.ReportExecutionProjectionTest",
                    AtfUnitTest.class),
            entry("fallback view shell", "atf.api.reporting.html.HtmlReportViewsTest",
                    AtfUnitTest.class),
            entry("fallback/Extent renderer parity",
                    "atf.api.reporting.html.ReportRendererParityTest",
                    AtfComponentTest.class),
            entry("typed environment snapshot", "atf.api.reporting.ui.UiExtentReportSessionCompositionTest",
                    AtfUnitTest.class),
            entry("operational System projection",
                    "atf.api.reporting.configuration.ReportEnvironmentSnapshotTest",
                    AtfUnitTest.class),
            entry("safe manual replay", "atf.api.reporting.api.ApiManualReplayProjectionTest",
                    AtfUnitTest.class),
            entry("semantic lifecycle boundary",
                    "atf.api.reporting.semantic.AtfReportSemanticSnapshotTest",
                    AtfUnitTest.class),
            entry("semantic snapshot", "atf.api.reporting.html.HtmlReportSemanticSnapshotMapperTest",
                    AtfUnitTest.class),
            entry("evidence markup", "atf.api.reporting.evidence.markup.ReportEvidenceMarkupTest",
                    AtfUnitTest.class),
            entry("asset publication", "atf.api.reporting.asset.ReportAssetContractTest",
                    AtfUnitTest.class),
            entry("asset size budget", "atf.api.reporting.asset.ReportAssetSizeBudgetTest",
                    AtfUnitTest.class),
            entry("typed screenshot capture provider",
                    "atf.api.reporting.evidence.profile.ReportEvidenceProfileTest",
                    AtfUnitTest.class),
            entry("capture provider publication",
                    "atf.api.reporting.ui.profile.ReportEvidenceProfileIntegrationTest",
                    AtfIntegrationTest.class),
            entry("runtime performance snapshot and budget",
                    "atf.api.reporting.runtime.performance.ReportRuntimePerformanceComponentTest",
                    AtfComponentTest.class),
            entry("UI component artifact", "atf.api.reporting.ui.contract.UiReportArtifactContractTest",
                    AtfUnitTest.class),
            entry("section component seam",
                    "atf.ui.tests.extent.component.audit.OverviewSecondaryContractValidatorTest",
                    AtfComponentTest.class),
            entry("configurable exact API selection projection",
                    "atf.ui.tests.extent.component.plan.ApiScenarioManifestTest",
                    AtfComponentTest.class),
            entry("Overview audit rules",
                    "atf.api.reporting.ui.audit.section.OverviewSectionValidatorTest",
                    AtfUnitTest.class),
            entry("Categories and request evidence rules",
                    "atf.api.reporting.ui.audit.section.CategoriesAndEvidenceSectionValidatorTest",
                    AtfUnitTest.class),
            entry("Fails projection rules",
                    "atf.api.reporting.ui.audit.section.FailsSectionValidatorTest",
                    AtfUnitTest.class),
            entry("Screen validation rules",
                    "atf.api.reporting.ui.audit.section.ScreenValidationSectionValidatorTest",
                    AtfUnitTest.class),
            entry("JSON and visual evidence rules",
                    "atf.ui.tests.extent.component.section.architecture.ExtractionValidationConvergenceTest",
                    AtfComponentTest.class),
            entry("Report projection rules",
                    "atf.api.reporting.ui.audit.section.ReportProjectionValidatorTest",
                    AtfUnitTest.class),
            entry("mock API integration", "atf.api.reporting.api.contract.MockApiReportIntegrationTest",
                    AtfIntegrationTest.class),
            entry("static UI browser proof",
                    "atf.ui.tests.extent.e2e.uireport.StaticUiExtentReportContractTest",
                    AtfE2eTest.class));

    @Test
    void everyPublishedBoundaryHasAnExplicitLowerLevelProof() {
        for (Entry entry : MATRIX) {
            Class<?> testClass = load(entry.className());
            assertTrue(entry.annotation().isAssignableFrom(annotationType(testClass)),
                    () -> entry.seam() + " must be marked with " + entry.annotation().getSimpleName()
                            + ": " + entry.className());
        }
    }

    @Test
    void eachExecutableLayerHasAStableLauncherBoundary() {
        assertEquals("atf.api.reporting",
                AtfReportingUnitSuite.class.getAnnotation(org.junit.platform.suite.api.SelectPackages.class)
                        .value()[0]);
        assertEquals("atf.api.reporting",
                AtfReportingComponentSuite.class.getAnnotation(org.junit.platform.suite.api.SelectPackages.class)
                        .value()[0]);
        assertEquals("atf.api.reporting",
                AtfReportingIntegrationSuite.class.getAnnotation(org.junit.platform.suite.api.SelectPackages.class)
                        .value()[0]);
        assertEquals("atf.api.tests.api",
                AtfReportingApiFixtureSuite.class.getAnnotation(org.junit.platform.suite.api.SelectPackages.class)
                        .value()[0]);
        assertEquals("atf.ui.tests.extent.e2e",
                AtfReportingUiAuditSuite.class.getAnnotation(org.junit.platform.suite.api.SelectPackages.class)
                        .value()[0]);
    }

    @Test
    void matrixHasOneReasonAndOneDeclaredProofPerBoundary() {
        assertEquals(MATRIX.size(), MATRIX.stream().map(Entry::seam).distinct().count());
        assertEquals(MATRIX.size(), MATRIX.stream().map(Entry::className).distinct().count());
        assertTrue(MATRIX.stream().allMatch(entry -> !entry.seam().isBlank()
                        && !entry.className().isBlank()),
                "every matrix row needs a readable reason and proof class");
    }

    private static Class<?> load(String name) {
        try {
            return Class.forName(name, false, ReportingCoverageMatrixTest.class.getClassLoader());
        } catch (ClassNotFoundException error) {
            throw new AssertionError("Missing coverage proof: " + name, error);
        }
    }

    private static Class<? extends Annotation> annotationType(Class<?> testClass) {
        if (testClass.isAnnotationPresent(AtfUnitTest.class)) return AtfUnitTest.class;
        if (testClass.isAnnotationPresent(AtfComponentTest.class)) return AtfComponentTest.class;
        if (testClass.isAnnotationPresent(AtfIntegrationTest.class)) return AtfIntegrationTest.class;
        if (testClass.isAnnotationPresent(AtfE2eTest.class)) return AtfE2eTest.class;
        throw new AssertionError("Coverage proof has no ATF layer annotation: " + testClass.getName());
    }

    private static Entry entry(String seam, String className,
                               Class<? extends Annotation> annotation) {
        return new Entry(seam, className, annotation);
    }

    private record Entry(String seam, String className,
                         Class<? extends Annotation> annotation) {
    }
}
