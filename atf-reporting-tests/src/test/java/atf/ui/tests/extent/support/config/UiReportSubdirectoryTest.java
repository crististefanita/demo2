package atf.ui.tests.extent.support.config;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfUnitTest
class UiReportSubdirectoryTest {
    private static final Path UI_REPORTS = Path.of("C:/atf/artifacts/reports/ui");

    @UiReportSubdirectory("e2e")
    private static final class E2eReportOwner { }

    private static final class DefaultReportOwner { }

    @Test
    void annotatedSuiteReportUsesRelativeE2eDirectory() {
        assertEquals(UI_REPORTS.resolve("e2e").toAbsolutePath().normalize(),
                ReportingTestResources.reportDirectory(UI_REPORTS, E2eReportOwner.class));
    }

    @Test
    void unannotatedReportsKeepTheExistingUiDirectory() {
        assertEquals(UI_REPORTS.toAbsolutePath().normalize(),
                ReportingTestResources.reportDirectory(UI_REPORTS, DefaultReportOwner.class));
        assertEquals(UI_REPORTS.toAbsolutePath().normalize(),
                ReportingTestResources.reportDirectory(UI_REPORTS, null));
    }

    @Test
    void annotationCannotEscapeTheUiReportDirectory() {
        assertThrows(IllegalStateException.class,
                () -> ReportingTestResources.reportDirectory(UI_REPORTS, TraversalReportOwner.class));
    }

    @UiReportSubdirectory("../api")
    private static final class TraversalReportOwner { }
}
