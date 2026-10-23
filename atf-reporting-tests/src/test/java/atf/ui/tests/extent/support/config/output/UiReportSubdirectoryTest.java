package atf.ui.tests.extent.support.config.output;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static atf.ui.tests.extent.support.config.output.UiReportOutputDirectory.resolve;

@AtfUnitTest
class UiReportSubdirectoryTest {
    private static final Path UI_REPORTS = Path.of("C:/atf/artifacts/reports/ui");

    @UiReportSubdirectory("e2e")
    private static final class E2eReportOwner { }

    private static final class DefaultReportOwner { }

    @Test
    void annotatedSuiteReportUsesRelativeE2eDirectory() {
        assertEquals(UI_REPORTS.resolve("e2e").toAbsolutePath().normalize(),
                resolve(UI_REPORTS, E2eReportOwner.class));
    }

    @Test
    void unannotatedReportsKeepTheExistingUiDirectory() {
        assertEquals(UI_REPORTS.toAbsolutePath().normalize(),
                resolve(UI_REPORTS, DefaultReportOwner.class));
        assertEquals(UI_REPORTS.toAbsolutePath().normalize(),
                resolve(UI_REPORTS, null));
    }

    @Test
    void annotationCannotEscapeTheUiReportDirectory() {
        assertThrows(IllegalStateException.class,
                () -> resolve(UI_REPORTS, TraversalReportOwner.class));
    }

    @Test
    void annotationCannotSelectAnAbsoluteDirectory() {
        assertThrows(IllegalStateException.class, () -> resolve(UI_REPORTS, AbsoluteReportOwner.class));
    }

    @Test
    void annotationCannotSelectABlankDirectory() {
        assertThrows(IllegalStateException.class, () -> resolve(UI_REPORTS, BlankReportOwner.class));
    }

    @UiReportSubdirectory("../api")
    private static final class TraversalReportOwner { }

    @UiReportSubdirectory("C:/outside-ui")
    private static final class AbsoluteReportOwner { }

    @UiReportSubdirectory("  ")
    private static final class BlankReportOwner { }
}
