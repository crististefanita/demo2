package atf.api.testkit.output;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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

    @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME)
    @UiReportSubdirectory("owner/ui")
    private @interface ComposedUiOutput { }

    @ComposedUiOutput private static class ComposedOwner { }
    @ComposedUiOutput @UiReportSubdirectory("owner/ui")
    private static class EquivalentOwner { }
    @ComposedUiOutput @ProjectRelativeUiReportDirectory("other")
    private static class ComposedConflictOwner { }

    @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME)
    @CycleB private @interface CycleA { }
    @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME)
    @CycleA @ProjectRelativeUiReportDirectory("cyclic")
    private @interface CycleB { }
    @CycleA private static class CyclicMetaOwner { }

    @ProjectRelativeUiReportDirectory("  ") private static class BlankOwner { }
    @ProjectRelativeUiReportDirectory("//server/share") private static class UncOwner { }
    @ProjectRelativeUiReportDirectory("../outside") private static class ProjectEscapeOwner { }
    @ProjectRelativeUiReportDirectory("owner/./project") private static class NormalizedOwner { }

    @Test
    void anchorsStayDistinctAndInheritanceCanBeOverridden() {
        Path ui = project.resolve("artifacts/reports/ui");
        assertEquals(ui.resolve("owner/ui"), ReportOutputDirectory.resolve(project, ui, UiOwner.class));
        assertEquals(project.resolve("owner/project"), ReportOutputDirectory.resolve(project, ui, ProjectOwner.class));
        assertEquals(project.resolve("child"), ReportOutputDirectory.resolve(project, ui, ChildOwner.class));
        assertEquals(ui.resolve("owner/ui"), ReportOutputDirectory.resolve(project, ui, ComposedOwner.class));
        assertEquals(ui.resolve("owner/ui"), ReportOutputDirectory.resolve(project, ui, EquivalentOwner.class));
        assertEquals(project.resolve("cyclic"), ReportOutputDirectory.resolve(project, ui, CyclicMetaOwner.class));
        assertEquals(project.resolve("owner/project"), ReportOutputDirectory.resolve(project, ui, NormalizedOwner.class));
        assertEquals(ui, ReportOutputDirectory.resolve(project, ui, null));
    }

    @Test
    void rejectsConflictAndUnsafePathsBeforePublication() {
        Path ui = project.resolve("artifacts/reports/ui");
        assertThrows(IllegalStateException.class, () -> ReportOutputDirectory.resolve(project, ui, ConflictOwner.class));
        assertThrows(IllegalStateException.class, () -> ReportOutputDirectory.resolve(project, ui, ComposedConflictOwner.class));
        assertThrows(IllegalStateException.class, () -> ReportOutputDirectory.resolve(project, ui, EscapeOwner.class));
        assertThrows(IllegalStateException.class, () -> ReportOutputDirectory.resolve(project, ui, DriveRelativeOwner.class));
        assertThrows(IllegalStateException.class, () -> ReportOutputDirectory.resolve(project, ui, UriOwner.class));
        assertThrows(IllegalStateException.class, () -> ReportOutputDirectory.resolve(project, ui, BlankOwner.class));
        assertThrows(IllegalStateException.class, () -> ReportOutputDirectory.resolve(project, ui, UncOwner.class));
        assertThrows(IllegalStateException.class, () -> ReportOutputDirectory.resolve(project, ui, ProjectEscapeOwner.class));
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
