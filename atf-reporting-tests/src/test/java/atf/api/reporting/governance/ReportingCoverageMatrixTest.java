package atf.api.reporting.governance;

import atf.api.testkit.AtfComponentTest;
import atf.api.testkit.AtfE2eTest;
import atf.api.testkit.AtfIntegrationTest;
import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keeps the reporting seam-to-test-level matrix executable and reviewable. */
@AtfComponentTest
class ReportingCoverageMatrixTest {
    private static final List<Entry> MATRIX = List.of(
            entry("semantic snapshot", "atf.api.reporting.html.HtmlReportSemanticSnapshotMapperTest",
                    AtfUnitTest.class),
            entry("evidence markup", "atf.api.reporting.evidence.markup.ReportEvidenceMarkupTest",
                    AtfUnitTest.class),
            entry("asset publication", "atf.api.reporting.asset.ReportAssetContractTest",
                    AtfUnitTest.class),
            entry("UI component artifact", "atf.api.reporting.ui.contract.UiReportArtifactContractTest",
                    AtfUnitTest.class),
            entry("section component seam",
                    "atf.ui.tests.extent.component.audit.OverviewSecondaryContractValidatorTest",
                    AtfComponentTest.class),
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
