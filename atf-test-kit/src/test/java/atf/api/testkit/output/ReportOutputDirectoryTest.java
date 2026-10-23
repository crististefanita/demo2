package atf.api.testkit.output;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfUnitTest
class ReportOutputDirectoryTest {
    @TempDir Path project;

    @UiReportSubdirectory("owner/ui")
    private static class UiOwner { }

    @ProjectRelativeUiReportDirectory("owner/project")
    private static class ProjectOwner { }

    @UiReportSubdirectory("base")
    private static class ParentOwner { }

    @ProjectRelativeUiReportDirectory("child")
    private static final class ChildOwner extends ParentOwner { }

    @UiReportSubdirectory("one")
    @ProjectRelativeUiReportDirectory("two")
    private static class ConflictOwner { }

    @UiReportSubdirectory("../escape")
    private static class EscapeOwner { }

    @ProjectRelativeUiReportDirectory("C:drive-relative")
    private static class DriveRelativeOwner { }

    @ProjectRelativeUiReportDirectory("https://example.invalid/reports")
    private static class UriOwner { }

    @Test
    void anchorsStayDistinctAndInheritanceCanBeOverridden() {
        Path ui = project.resolve("artifacts/reports/ui");
        assertEquals(ui.resolve("owner/ui"), ReportOutputDirectory.resolve(project, ui, UiOwner.class));
        assertEquals(project.resolve("owner/project"), ReportOutputDirectory.resolve(project, ui, ProjectOwner.class));
        assertEquals(project.resolve("child"), ReportOutputDirectory.resolve(project, ui, ChildOwner.class));
        assertEquals(ui, ReportOutputDirectory.resolve(project, ui, null));
    }

    @Test
    void rejectsConflictAndUnsafePathsBeforePublication() {
        Path ui = project.resolve("artifacts/reports/ui");
        assertThrows(IllegalStateException.class, () -> ReportOutputDirectory.resolve(project, ui, ConflictOwner.class));
        assertThrows(IllegalStateException.class, () -> ReportOutputDirectory.resolve(project, ui, EscapeOwner.class));
        assertThrows(IllegalStateException.class, () -> ReportOutputDirectory.resolve(project, ui, DriveRelativeOwner.class));
        assertThrows(IllegalStateException.class, () -> ReportOutputDirectory.resolve(project, ui, UriOwner.class));
    }

    @Test
    void symlinkCannotEscapeExistingAnchor() throws Exception {
        Path ui = Files.createDirectories(project.resolve("artifacts/reports/ui"));
        Path outside = Files.createDirectories(project.resolve("outside"));
        assumeTrue(ReportOutputSymlinkFixture.createIfSupported(ui.resolve("owner"), outside),
                "OS does not allow creating a test symlink");
        assertThrows(IllegalStateException.class,
                () -> ReportOutputDirectory.resolve(project, ui, UiOwner.class));
    }
}
